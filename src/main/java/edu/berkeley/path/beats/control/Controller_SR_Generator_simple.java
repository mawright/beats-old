package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.actuator.ActuatorCMS;
import edu.berkeley.path.beats.jaxb.*;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 2/5/14.
 */
public class Controller_SR_Generator_simple extends Controller {

    protected List<NodeData> node_data;

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_SR_Generator_simple(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c) {
        super(myScenario,c, Controller.Algorithm.SR_Generator);
    }

    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset  / update
    /////////////////////////////////////////////////////////////////////

    @Override
    protected void populate(Object jaxbobject) {

        // load offramp flow information
        Parameters param = (Parameters) ((edu.berkeley.path.beats.jaxb.Controller)jaxbobject).getParameters();
        DemandSet demand_set = null;
        try {
            demand_set = Jaxb.create_demand_set_from_xml(param.get("fr_flow_file"));
        } catch (BeatsException e) {
            e.printStackTrace();
            return;
        }
        demand_set.populate(myScenario);
        node_data = new ArrayList<NodeData>();
        for(Actuator act:actuators){
            ScenarioElement se = (ScenarioElement) act.getScenarioElement();
            if(se.getMyType()!=ScenarioElement.Type.node)
                continue;
            node_data.add(new NodeData(demand_set,(Node) se.getReference()));
        }
    }

    @Override
    protected void validate() {

        // check node data
        for(Actuator act:actuators){
            ScenarioElement se = (ScenarioElement) act.getScenarioElement();
            if(se.getMyType()!=ScenarioElement.Type.node)
                BeatsErrorLog.addError("In Controller_SR_Generator, all actuators must be on nodes.");
        }

        for(NodeData nd : node_data){
            if(nd.link_fw_dn.size()!=1)
                BeatsErrorLog.addError("In Controller_SR_Generator, must have exactly one downstream mainline link.");
            if(nd.link_fw_up.size()!=1)
                BeatsErrorLog.addError("In Controller_SR_Generator, must have exactly one upstream mainline link.");
            if(nd.link_fr.size()<1)
                BeatsErrorLog.addError("In Controller_SR_Generator, must have at least one offramp link.");
        }
    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void update() throws BeatsException {

        double beta;
        double dt_in_hr = myScenario.getSimdtinseconds()/3600d;

        for(int i=0;i<node_data.size();i++){
            NodeData nd = node_data.get(i);
            double [] fr_flow_vph = nd.get_fr_flow_in_vph();
            double tot_fr_flow_vph = BeatsMath.sum(fr_flow_vph);
            double ml_up_demand_vph = BeatsMath.sum(nd.link_fw_up.get(0).get_out_demand_in_veh(0))/dt_in_hr;
            double ml_dn_supply_vph = 0d;
            for(Link link : nd.link_fr)
                ml_dn_supply_vph += link.get_space_supply_in_veh(0);
            ml_dn_supply_vph /= dt_in_hr;
            double ml_up_flow_vph = Math.min( ml_up_demand_vph , ml_dn_supply_vph + tot_fr_flow_vph );
            for(int j=0;j<nd.link_fr.size();j++){

                if(BeatsMath.equals(fr_flow_vph[j],0d))
                    beta = 0d;
                else if(BeatsMath.equals(ml_up_flow_vph,0d))
                    beta = 1d;
                else
                    beta = Math.min( fr_flow_vph[j] / ml_up_flow_vph , 1d );

//                for(VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType()){
//                    ((ActuatorCMS)actuators.get(i)).set_split( nd.fw_dn_id(0) , nd.fr_id(j) , vt.getId() , beta );
//                    try{
//                        writer.write(String.format("%.1f\t%d\t%d\t%d\t%d\t%f\t%f\t%f\n",myScenario.getCurrentTimeInSeconds(),nd.getId(),nd.fw_up_id(0),nd.fr_id(j),vt.getId(),fr_flow_vph[j],ml_up_flow_vph,beta));
//                    }
//                    catch(IOException e){
//                        System.err.print(e);
//                    }
//                }
            }
        }
    }


    class NodeData {

        protected long id;
        protected int step_initial_abs;
        protected boolean isdone;
        protected List<BeatsTimeProfile> fr_flow;	// [veh] demand profile per vehicle type
        protected List<Link> link_fw_up;
        protected List<Link> link_fw_dn;
        protected List<Link> link_fr;

        public NodeData(DemandSet demand_set,Node myNode){

            this.id = myNode.getId();

            link_fw_up = new ArrayList<Link>();
            for(Link link : myNode.getInput_link())
                if(link.isFreeway())
                    link_fw_up.add(link);

            link_fw_dn = new ArrayList<Link>();
            link_fr = new ArrayList<Link>();
            for(Link link : myNode.getOutput_link()){
                if(link.isFreeway())
                    link_fw_dn.add(link);
                if(link.isOfframp())
                    link_fr.add(link);
            }

            // find the demand profile for the offramps
            fr_flow = new ArrayList<BeatsTimeProfile>();
            List<Double> start_time = new ArrayList<Double>();
            for(Link link : link_fr){
                edu.berkeley.path.beats.jaxb.DemandProfile dp = demand_set.get_demand_profile_for_link_id(link.getId());
                if(dp==null)
                    fr_flow.add(new BeatsTimeProfile("0",true));
                else{
                    fr_flow.add(new BeatsTimeProfile(dp.getDemand().get(0).getContent(),true));
                    start_time.add(Double.isInfinite(dp.getStartTime()) ? 0d : dp.getStartTime());
                }
            }

            // check all starttimes are the same
            boolean all_same = true;
            if(!start_time.isEmpty()){
                double first = start_time.get(0);
                for(Double d : start_time)
                    if(d!=first)
                        all_same = false;
            }
            else
                start_time.add(0d);
            if(!all_same)
                start_time = null;
            else{
                step_initial_abs = BeatsMath.round(start_time.get(0)/myScenario.getSimdtinseconds());
                isdone = false;
            }

        }

        protected double [] get_fr_flow_in_vph(){

            double [] val = new double [link_fr.size()];

            for(int i=0;i<link_fr.size();i++){

                BeatsTimeProfile profile = fr_flow.get(i);

                if( !isdone && myScenario.getClock().is_time_to_sample_abs(samplesteps, step_initial_abs)){

                    // REMOVE THESE
                    int n = profile.getNumTime()-1;
                    int step = myScenario.getClock().sample_index_abs(samplesteps,step_initial_abs);

                    // demand is zero before step_initial_abs
                    if(myScenario.getClock().getAbsoluteTimeStep()< step_initial_abs)
                        val[i] = 0d;

                    // sample the profile
                    if(step<n)
                        val[i] = profile.get(step);

                    // last sample
                    if(step>=n && !isdone){
                        isdone = true;
                        val[i] = profile.get(n);
                    }
                }
                val[i] = Math.abs(val[i]);
            }
            return val;
        }

        protected long getId(){
            return id;
        }
        protected long fw_up_id(int index){
            return link_fw_up.get(index).getId();
        }
        protected long fw_dn_id(int index){
            return link_fw_dn.get(index).getId();
        }
        protected long fr_id(int index){
            return link_fr.get(index).getId();
        }

    }
}
