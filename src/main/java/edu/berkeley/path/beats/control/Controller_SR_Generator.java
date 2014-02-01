package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.actuator.ActuatorCMS;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Parameters;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 1/31/14.
 */
public class Controller_SR_Generator extends Controller {

    protected List<NodeData> node_data;

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_SR_Generator(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c) {
        super(myScenario,c,Algorithm.SR_Generator);
    }


    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset  / update
    /////////////////////////////////////////////////////////////////////


    @Override
    protected void populate(Object jaxbobject) {

        // load offramp flow information
        Parameters param = (Parameters) ((edu.berkeley.path.beats.jaxb.Controller)jaxbobject).getParameters();
        String configfilename = param.get("fr_flow_file");

        JAXBContext context;
        Unmarshaller u = null;

        // create unmarshaller .......................................................
        try {
            //Reset the classloader for main thread; need this if I want to run properly
            //with JAXB within MATLAB. (luis)
            Thread.currentThread().setContextClassLoader(ObjectFactory.class.getClassLoader());
            context = JAXBContext.newInstance("edu.berkeley.path.beats.jaxb");
            u = context.createUnmarshaller();
        } catch( JAXBException je ) {
            System.err.print("Failed to create context for JAXB unmarshaller");
        }

        // schema assignment ..........................................................
        try{
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            ClassLoader classLoader = ObjectFactory.class.getClassLoader();
            Schema schema = factory.newSchema(classLoader.getResource("beats.xsd"));
            u.setSchema(schema);
        } catch(SAXException e){
            System.err.print("Schema not found");
        }

        // process configuration file name ...........................................
        if(!configfilename.endsWith(".xml"))
            configfilename += ".xml";

        // read and return ...........................................................
        DemandSet demand_set = null; //new Scenario();
        try {
            ObjectFactory.setObjectFactory(u, new JaxbObjectFactory());
            demand_set = (DemandSet) u.unmarshal( new FileInputStream(configfilename) );
        } catch( JAXBException je ) {
            System.err.print("JAXB threw an exception when loading the configuration file");
        } catch (FileNotFoundException e) {
            System.err.print("Configuration file not found");
        }

        if(demand_set==null)
            return;


        node_data = new ArrayList<NodeData>();
        for(Actuator act:actuators){
            ScenarioElement se = (ScenarioElement) act.getScenarioElement();

            if(se.getMyType().compareTo(ScenarioElement.Type.node)!=0)
                continue;

            Node myNode = (Node) se.getReference();
            if(myNode.getnIn()!=1 || myNode.getnOut()!=2)
                continue;
            Link o0 = myNode.getOutput_link()[0];
            Link o1 = myNode.getOutput_link()[1];
            Link o_fw=null;
            Link o_fr=null;
            if(o0.isFreeway() && o1.isOfframp()){
                o_fw = o0;
                o_fr = o1;
            } else if ( o1.isFreeway() && o0.isOfframp() ){
                o_fw = o1;
                o_fr = o0;
            }

            // find the demand profile for this link
            DemandProfile mydp = null;
            for(DemandProfile dp : demand_set.getDemandProfile())
                if(dp.getLinkIdOrg()==o_fr.getId()){
                    mydp = dp;
                    break;
                }

            node_data.add(new NodeData(mydp,myNode.getInput_link()[0],o_fw,o_fr));
        }
    }

    @Override
    protected void validate() {

        // check node data
        for(Actuator act:actuators){

            ScenarioElement se = (ScenarioElement) act.getScenarioElement();

            if(se.getMyType().compareTo(ScenarioElement.Type.node)!=0)
                BeatsErrorLog.addError("In Controller_SR_Generator, all actuators must be on nodes.");

            Node myNode = (Node) se.getReference();

            if(myNode.getnIn()!=1)
                BeatsErrorLog.addError("In Controller_SR_Generator, actuated node must have nIn=1.");

            if(myNode.getnOut()!=2)
                BeatsErrorLog.addError("In Controller_SR_Generator, actuated node must have nOut=2.");
        }

        for(NodeData nd : node_data){
            if(nd.link_ml_up==null)
                BeatsErrorLog.addError("In Controller_SR_Generator, bad upstream link.");
            if(nd.link_ml_dn==null)
                BeatsErrorLog.addError("In Controller_SR_Generator, bad downstream link.");
            if(nd.link_fr==null)
                BeatsErrorLog.addError("In Controller_SR_Generator, bad offramp link.");
        }

    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void update() throws BeatsException {

        for(int i=0;i<node_data.size();i++){
            NodeData nd = node_data.get(i);
            double fr_flow = nd.get_fr_flow_at_time(myScenario.getClock().getStartTime());
            double ml_up_demand = BeatsMath.sum(nd.link_ml_up.getOutflowDemand(0));
            double ml_dn_supply = nd.link_ml_dn.getSpaceSupply(0);
            double ml_up_flow = Math.min( ml_up_demand , ml_dn_supply + fr_flow );
            double beta = Math.min( fr_flow / ml_up_flow , 1d );

            for(VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType()){
                ((ActuatorCMS)actuators.get(i)).set_split( nd.link_ml_up.getId() ,
                                              nd.link_fr.getId(),
                                              vt.getId(),
                                              beta);
            }
        }


    }


    class NodeData {

        int step_initial_abs;
        boolean isdone;
        double current_value;

        private BeatsTimeProfile fr_flow;	// [veh] demand profile per vehicle type

        protected DemandProfile profile;
        protected Link link_ml_up;
        protected Link link_ml_dn;
        protected Link link_fr;

        public NodeData(DemandProfile dp,Link link_ml_up,Link link_ml_dn,Link link_fr){

            if(dp==null)
                return;

            // limit case to single vehicle type
            if(dp.getDemand().size()!=1)
                System.err.print("Offramp demand profiles only allowed in no-commodity flow case.");

            this.fr_flow = new BeatsTimeProfile(dp.getDemand().get(0).getContent(),true);
            this.link_ml_up = link_ml_up;
            this.link_ml_dn = link_ml_dn;
            this.link_fr = link_fr;


            // step_initial
            double start_time = Double.isInfinite(dp.getStartTime()) ? 0d : dp.getStartTime();
            step_initial_abs = BeatsMath.round(start_time/myScenario.getSimdtinseconds());
            isdone = false;
            current_value = 0d;

        }

        protected double get_fr_flow_at_time(double time_in_seconds){

            if( !isdone && myScenario.getClock().is_time_to_sample_abs(samplesteps, step_initial_abs)){

                // REMOVE THESE
                int n = fr_flow.getNumTime()-1;
                int step = myScenario.getClock().sample_index_abs(samplesteps,step_initial_abs);

                // demand is zero before step_initial_abs
                if(myScenario.getClock().getAbsoluteTimeStep()< step_initial_abs)
                    current_value = 0d;

                // sample the profile
                if(step<n){
                    current_value = fr_flow.get(myScenario.getClock().sample_index_abs(samplesteps,step_initial_abs));
                }

                // last sample
                if(step>=n && !isdone){
                    isdone = true;
                    current_value = fr_flow.get(n);
                }
            }

            return current_value;
        }


    }

}
