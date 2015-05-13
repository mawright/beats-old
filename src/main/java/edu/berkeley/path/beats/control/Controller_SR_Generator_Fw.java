package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.actuator.ActuatorCMS;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Parameters;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;
import edu.berkeley.path.beats.simulator.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 1/31/14.
 */
public class Controller_SR_Generator_Fw extends Controller {

    private List<NodeData> node_data;
    protected int logger_id;
    protected int dt_log = 300;   // sec
    protected boolean in_fr_demands_mode;

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_SR_Generator_Fw(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c) {
        super(myScenario,c,Algorithm.SR_Generator);
        in_fr_demands_mode = myScenario.runMode== Scenario.RunMode.FRDEMANDS;
    }

    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset  / update
    /////////////////////////////////////////////////////////////////////

    @Override
    protected void populate(Object jaxbobject) {

        if(!in_fr_demands_mode)
            return;

        // load offramp flow information
        Parameters param = (Parameters) ((edu.berkeley.path.beats.jaxb.Controller)jaxbobject).getParameters();
        String configfilename = getConfigFilename(param);

        // read and return ...........................................................
        DemandSet demand_set = null;
        try {
            demand_set = Jaxb.create_demand_set_from_xml(configfilename);
        } catch (BeatsException e) {
            e.printStackTrace();
            return;
        }

        if(demand_set==null)
            return;

        demand_set.populate(myScenario);

        initializeNodeData();
        for(Actuator act:actuators){
            ScenarioElement se = (ScenarioElement) act.getScenarioElement();
            if(se.getMyType()!=ScenarioElement.Type.node)
                continue;
            appendNodeData(demand_set, se, myScenario);
        }

        // logger
        String log_file = param.get("log_file");
        if(log_file!=null)
            logger_id = DebugLogger.add_writer(log_file);
    }

    protected String getConfigFilename(Parameters param) {
        return param.get("fr_flow_file");
    }

    protected void initializeNodeData(){
        node_data = new ArrayList<NodeData>();
    }

    protected void appendNodeData(DemandSet demand_set, ScenarioElement se, Scenario scneario) {
        node_data.add(new NodeData(demand_set, (Node) se.getReference(), scneario));
    }

    @Override
    protected void validate() {

        if(!in_fr_demands_mode)
            return;

        // check node data
        for(Actuator act:actuators){
            ScenarioElement se = (ScenarioElement) act.getScenarioElement();
            if(se.getMyType()!=ScenarioElement.Type.node)
                BeatsErrorLog.addError("In Controller_SR_Generator, all actuators must be on nodes.");
        }

//        for(NodeData nd : node_data){
//            if(nd.link_fw_dn.size()!=1)
//                BeatsErrorLog.addError("In Controller_SR_Generator, must have exactly one downstream mainline link.");
//            if(nd.link_fw_up.size()!=1)
//                BeatsErrorLog.addError("In Controller_SR_Generator, must have exactly one upstream mainline link.");
//            if(nd.fr.size()<1)
//                BeatsErrorLog.addError("In Controller_SR_Generator, must have at least one offramp link.");
//        }
    }

    @Override
    protected void reset() {
        if(!in_fr_demands_mode)
            return;
        super.reset();
    }

    @Override
    protected void update() throws BeatsException {

        if(!in_fr_demands_mode)
            return;

        for(int n=0;n<node_data.size();n++){
            NodeData nd = node_data.get(n);

            // update node information
            nd.update_info();

            // OR->FR => beta = 0;
            if(nd.or!=null) {
                for (VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType())
                    if (getMyScenario().get.currentTimeInSeconds() % dt_log == 0) {
                        DebugLogger.write(logger_id, String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                                getMyScenario().get.currentTimeInSeconds(),
                                nd.getId(),
                                nd.or.getId(),
                                nd.fr.getId(),
                                vt.getId(),
                                0d));

                        ((ActuatorCMS) actuators.get(n)).set_split(
                                nd.or.getId(),
                                nd.fr.getId(),
                                vt.getId(),
                                0d);
                    }
            }

            // ML->FR, compute beta
            for(VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType())
                if(getMyScenario().get.currentTimeInSeconds()%dt_log==0){
                    DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                            getMyScenario().get.currentTimeInSeconds(),
                            nd.getId(),
                            nd.up_ml.getId(),
                            nd.fr.getId(),
                            vt.getId(),
                            nd.beta));

                    ((ActuatorCMS)actuators.get(n)).set_split(
                            nd.up_ml.getId() ,
                            nd.fr.getId() ,
                            vt.getId() ,
                            nd.beta );
                }

            // ML->ML, adjust
            for(int k=0;k<myScenario.get.numVehicleTypes();k++){
                if(getMyScenario().get.currentTimeInSeconds()%dt_log==0)
                    DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                            getMyScenario().get.currentTimeInSeconds(),
                            nd.getId(),
                            nd.up_ml.getId(),
                            nd.dn_ml.getId(),
                            myScenario.get.vehicleTypeIdForIndex(k),
                            1 - nd.beta));

                ((ActuatorCMS)actuators.get(n)).set_split(
                        nd.up_ml.getId() ,
                        nd.dn_ml.getId() ,
                        myScenario.get.vehicleTypeIdForIndex(k) ,
                        1-nd.beta );
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // api
    /////////////////////////////////////////////////////////////////////

    public void setKnobForLink(long link_id,double newknob){

        // get NodeData corresponding to this link
        NodeData node = null;
        for(NodeData N : node_data){
            if(N.fr.getId()==link_id) {
                node = N;
                break;
            }
        }

        if(node==null)
            return;

        // set knob in node
        node.set_knob_for_fr_link(newknob);

    }

    /////////////////////////////////////////////////////////////////////
    // inner classes
    /////////////////////////////////////////////////////////////////////

    class NodeData {

        private Node myNode;

        private long id;
        private int step_initial_abs = 0;
        private boolean isdone = false;
        private double knob;
        private BeatsTimeProfile fr_flow;	// [veh] demand profile per vehicle type

        private Link or;
        private Link up_ml;
        private Link fr;
        private Link dn_ml;

        private double fr_flow_veh;
        protected double beta;

        public NodeData(DemandSet demand_set,Node myNode, Scenario scenario){

            this.id = myNode.getId();
            this.myNode = myNode;

            //  link references
            for(int i=0;i<myNode.getnIn();i++){
                Link link = myNode.getInput_link()[i];
                if(link.isOnramp())
                    or = link;
                else
                    up_ml = link;
            }

            for(int j=0;j<myNode.getnOut();j++){
                Link link = myNode.getOutput_link()[j];
                if(link.isOfframp())
                    fr = link;
                else
                    dn_ml = link;
            }

            // find the demand profile for the offramp
            DemandProfile dp = demand_set.get_demand_profile_for_link_id(fr.getId());
            if(dp==null) {
                knob = 1d;
                fr_flow = new BeatsTimeProfile("0", true, scenario);
            }
            else{
                knob = dp.getKnob();
                fr_flow = new BeatsTimeProfile(dp.getDemand().get(0).getContent(),true, scenario);
            }


        }

        public void update_info(){

            // fr_flow_veh
            fr_flow_veh = get_fr_flow_in_veh();

            if(BeatsMath.equals(fr_flow_veh,0d)){
                beta = 0d;
                return;
            }

            // Sml [veh]
            double Sml = BeatsMath.sum(up_ml.get_out_demand_in_veh(0));
            if(BeatsMath.equals(Sml,0d)){
                beta = 1d;
                return;
            }

            // Rml
            double Rml = dn_ml.get_available_space_supply_in_veh(0);
            if(BeatsMath.equals(Rml,0d)){
                beta = 1d;
                return;
            }

            double Sor = or!=null ? BeatsMath.sum( or.get_out_demand_in_veh(0) ) : 0d;
            double num = Sor + Sml;
            double den = Rml + fr_flow_veh;
            beta = Math.min( 1d , (fr_flow_veh/Sml)*Math.max(1d,num/den) );

        }

        protected double get_fr_flow_in_veh(){

            double val = 0d;

            if(fr_flow.isEmpty())
                return val;

            int prof_sample_steps = fr_flow.getSampleSteps();

            if( !isdone && myScenario.get.clock().is_time_to_sample_abs(prof_sample_steps, step_initial_abs)){

                // REMOVE THESE
                int n = fr_flow.getNumTime()-1;
                int step = myScenario.get.clock().sample_index_abs(prof_sample_steps,step_initial_abs);

                // demand is zero before step_initial_abs
                if(myScenario.get.clock().getAbsoluteTimeStep()< step_initial_abs)
                    val = 0d;

                // sample the profile
                if(step<n)
                    val = fr_flow.get(step);

                // last sample
                if(step>=n && !isdone){
                    isdone = true;
                    val = fr_flow.get(n);
                }
                val = Math.abs(val);
                val *= myScenario.get.simdtinseconds();
                val *= knob;

                return val;
            }
            else
                return this.fr_flow_veh;
        }

        protected long getId(){
            return id;
        }

        protected void set_knob_for_fr_link(double newknob){
            knob = newknob;
        }
    }

}
