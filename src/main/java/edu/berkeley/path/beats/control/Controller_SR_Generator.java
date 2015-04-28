package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.actuator.ActuatorCMS;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Parameters;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;
import edu.berkeley.path.beats.simulator.utils.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 1/31/14.
 */
public class Controller_SR_Generator extends Controller {

    private List<NodeData> node_data;
    protected double dt_in_hr;
    protected int logger_id;
    protected int dt_log = 300;   // sec

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
            appendNodeData(demand_set, se);
        }

        dt_in_hr = myScenario.getSimdtinseconds()/3600d;

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

    protected void appendNodeData(DemandSet demand_set, ScenarioElement se) {
        node_data.add(new NodeData(demand_set, (Node) se.getReference()));
    }

    @Override
    protected void validate() {

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
//            if(nd.link_fr.size()<1)
//                BeatsErrorLog.addError("In Controller_SR_Generator, must have at least one offramp link.");
//        }
    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void update() throws BeatsException {

        for(int n=0;n<node_data.size();n++){
            NodeData nd = node_data.get(n);

            // update node informatio
            nd.update_info();

            // OR->FR => beta = 0;
            for(int i=0;i<nd.ind_or.size();i++)
                for(int j=0;j<nd.ind_fr.size();j++)
                    for(VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType())

                        if(getMyScenario().getCurrentTimeInSeconds()%dt_log==0){
                            DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                                    getMyScenario().getCurrentTimeInSeconds(),
                                    nd.getId(),
                                    nd.link_or.get(i).getId(),
                                    nd.link_fr.get(j).getId(),
                                    vt.getId(),
                                    0d));

                            ((ActuatorCMS)actuators.get(n)).set_split(
                                    nd.link_or.get(i).getId(),
                                    nd.link_fr.get(j).getId(),
                                    vt.getId(),
                                    0d);
                        }

            // !OR->FR, compute beta
            double beta = nd.offramp_flow_demand_ratio;
            for(int j=0;j<nd.non_offramp_xi.length;j++)
                beta = Math.max(beta,nd.non_offramp_xi[j]);
            beta = Math.min(beta,1d);

            for(int i=0;i<nd.ind_not_or.size();i++)
                for(int j=0;j<nd.ind_fr.size();j++)
                    for(VehicleType vt : myScenario.getVehicleTypeSet().getVehicleType())

                        if(getMyScenario().getCurrentTimeInSeconds()%dt_log==0){
                            DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                                    getMyScenario().getCurrentTimeInSeconds(),
                                    nd.getId(),
                                    nd.link_not_or.get(i).getId(),
                                    nd.link_fr.get(j).getId(),
                                    vt.getId(),
                                    beta ));

                            ((ActuatorCMS)actuators.get(n)).set_split(
                                    nd.link_not_or.get(i).getId() ,
                                    nd.link_fr.get(j).getId() ,
                                    vt.getId() ,
                                    beta );
                        }

            // !OR->!FR, adjust
            for(int i=0;i<nd.ind_not_or.size();i++){
                int ii = nd.ind_not_or.get(i);
                for(int j=0;j<nd.ind_not_fr.size();j++){
                    int jj = nd.ind_not_fr.get(j);
                    for(int k=0;k<myScenario.getNumVehicleTypes();k++){
                        double alpha = nd.get_sr(ii,jj,k);
                        double r = (1d-beta)/nd.non_onramp_splits[i][k];

                        if(getMyScenario().getCurrentTimeInSeconds()%dt_log==0)
                            DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                                    getMyScenario().getCurrentTimeInSeconds(),
                                    nd.getId(),
                                    nd.link_not_or.get(i).getId() ,
                                    nd.link_not_fr.get(j).getId() ,
                                    myScenario.getVehicleTypeIdForIndex(k) ,
                                    r*alpha ));

                        ((ActuatorCMS)actuators.get(n)).set_split(
                                nd.link_not_or.get(i).getId() ,
                                nd.link_not_fr.get(j).getId() ,
                                myScenario.getVehicleTypeIdForIndex(k) ,
                                r*alpha );
                    }
                }
            }
        }
    }

    class NodeData {

        Node myNode;

        protected long id;
        protected int step_initial_abs;
        protected boolean isdone;
        protected List<BeatsTimeProfile> fr_flow;	// [veh] demand profile per vehicle type

        protected List<Link> link_or;
        protected List<Link> link_not_or;
        protected List<Link> link_fr;
        protected List<Link> link_not_fr;

        protected List<Integer> ind_or;
        protected List<Integer> ind_not_or;
        protected List<Integer> ind_fr;
        protected List<Integer> ind_not_fr;

        double total_non_onramp_demand;
        double [] known_non_offramp_demand;
        double [][] non_onramp_splits;
        double [] non_offramp_phi;
        double offramp_flow_demand_ratio;
        double [] non_offramp_xi;
        double [] fr_flow_veh;

        public NodeData(DemandSet demand_set,Node myNode){

            this.id = myNode.getId();
            this.myNode = myNode;

            //  link references
            link_or = new ArrayList<Link>();
            link_not_or = new ArrayList<Link>();
            ind_or = new ArrayList<Integer>();
            ind_not_or = new ArrayList<Integer>();
            for(int i=0;i<myNode.getnIn();i++){
                Link link = myNode.getInput_link()[i];
                if(link.isOnramp()){
                    link_or.add(link);
                    ind_or.add(i);
                }
                else{
                    link_not_or.add(link);
                    ind_not_or.add(i);
                }
            }

            link_fr = new ArrayList<Link>();
            link_not_fr = new ArrayList<Link>();
            ind_fr = new ArrayList<Integer>();
            ind_not_fr = new ArrayList<Integer>();
            for(int j=0;j<myNode.getnOut();j++){
                Link link = myNode.getOutput_link()[j];
                if(link.isOfframp()){
                    link_fr.add(link);
                    ind_fr.add(j);
                }
                else{
                    link_not_fr.add(link);
                    ind_not_fr.add(j);
                }
            }

            // find the demand profile for the offramps
            fr_flow = new ArrayList<BeatsTimeProfile>();
            List<Double> start_time = new ArrayList<Double>();
            for(Link link : link_fr){
                DemandProfile dp = demand_set.get_demand_profile_for_link_id(link.getId());
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
                step_initial_abs = BeatsMath.round(start_time.get(0) / myScenario.getSimdtinseconds());
                isdone = false;
            }

        }

        public void update_info(){

            int i,j,k;

            // total_non_onramp_demand [veh]
            total_non_onramp_demand = 0d;
            for(Link link : link_not_or)
                total_non_onramp_demand += BeatsMath.sum(link.get_out_demand_in_veh(0));

            // known_non_offramp_demand [veh]
            known_non_offramp_demand = new double[link_not_fr.size()];
            for(j=0;j<ind_not_fr.size();j++){
                known_non_offramp_demand[j] = 0d;
                int jj = ind_not_fr.get(j);
                for(i=0;i<ind_or.size();i++){
                    int ii = ind_or.get(i);
                    double [] Si = link_or.get(i).get_out_demand_in_veh(0);
                    for(k=0;k<myScenario.getNumVehicleTypes();k++)
                        known_non_offramp_demand[j] += Si[k]*get_sr(ii,jj,k);
                }
            }

            // non_onramp_splits
            non_onramp_splits = new double[link_not_or.size()][myScenario.getNumVehicleTypes()];
            for(i=0;i<ind_not_or.size();i++){
                int ii = ind_not_or.get(i);
                for(j=0;j<ind_not_fr.size();j++){
                    int jj = ind_not_fr.get(j);
                    for(k=0;k<myScenario.getNumVehicleTypes();k++)
                        non_onramp_splits[i][k] += get_sr(ii,jj,k);
                }
            }

            // non_offramp_phi
            non_offramp_phi = new double[link_not_fr.size()];
            for(j=0;j<ind_not_fr.size();j++){
                int jj = ind_not_fr.get(j);
                for(i=0;i<ind_not_or.size();i++){
                    int ii = ind_not_or.get(i);
                    double [] Si = link_not_or.get(i).get_out_demand_in_veh(0);
                    for(k=0;k<myScenario.getNumVehicleTypes();k++){
                        double alpha = get_sr(ii,jj,k);
                        non_offramp_phi[j] += alpha*Si[k]/non_onramp_splits[i][k];
                    }
                }
            }

            // fr_flow_veh
            fr_flow_veh = get_fr_flow_in_veh();
            double tot_fr_flow_veh = BeatsMath.sum(fr_flow_veh);

            // offramp_flow_demand_ratio
            if(BeatsMath.equals(tot_fr_flow_veh,0d))
                offramp_flow_demand_ratio = 0d;
            else if(BeatsMath.greaterthan(tot_fr_flow_veh,total_non_onramp_demand))
                offramp_flow_demand_ratio = 1d;
            else
                offramp_flow_demand_ratio = tot_fr_flow_veh / total_non_onramp_demand;

            // non_offramp_xi
            non_offramp_xi = new double[link_not_fr.size()];
            for(j=0;j<ind_not_fr.size();j++){
                double num = offramp_flow_demand_ratio*(known_non_offramp_demand[j] + non_offramp_phi[j]);
                double Rj = link_not_fr.get(j).get_total_space_supply_in_veh(0);
                double den = Rj + offramp_flow_demand_ratio * non_offramp_phi[j];
                if(BeatsMath.equals(num,0d))
                    non_offramp_xi[j] = 0d;
                else if(BeatsMath.greaterthan(num,den))
                    non_offramp_xi[j] = 1d;
                else
                    non_offramp_xi[j] = num / den;
            }

        }

        public double get_sr(int ii,int jj,int kk){
            return myNode.getSplitRatioProfileValue(ii,jj,kk);
        }

        protected double [] get_fr_flow_in_veh(){

            double [] val = new double [link_fr.size()];
            int prof_sample_steps = 60;         ///// HACK!!!!

            if( !isdone && myScenario.getClock().is_time_to_sample_abs(prof_sample_steps, step_initial_abs)){

                for(int i=0;i<link_fr.size();i++){

                    BeatsTimeProfile profile = fr_flow.get(i);

                    // REMOVE THESE
                    int n = profile.getNumTime()-1;
                    int step = myScenario.getClock().sample_index_abs(prof_sample_steps,step_initial_abs);

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
                    val[i] = Math.abs(val[i]);
                    val[i] *= dt_in_hr;

                }
                return val;
            }
            else
                return this.fr_flow_veh;
        }

        protected long getId(){
            return id;
        }

    }

}
