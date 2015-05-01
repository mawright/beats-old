package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.actuator.ActuatorCMS;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Parameters;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.path.beats.simulator.utils.BeatsException;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;
import edu.berkeley.path.beats.simulator.utils.BeatsTimeProfile;
import edu.berkeley.path.beats.simulator.utils.DebugLogger;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;

/**
 * Created by mwright on 4/11/15.
 */
public class Controller_HOV_SR_Generator extends Controller_SR_Generator {

	private long hov_vtype_id;
	protected int hov_vtype_index;

	private List<HOVNodeData> node_data;

	public Controller_HOV_SR_Generator(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c) {
		super(myScenario,c);
		hov_vtype_index = myScenario.get.vehicleTypeIndexForName("HOV");
		hov_vtype_id = myScenario.get.vehicleTypeIdForIndex(hov_vtype_index);
		dt_in_hr = myScenario.get.simdtinseconds()/3600d;
	}

	@Override
	protected String getConfigFilename(Parameters param) {
		return param.get("hov_flow_file");
	}

	@Override
	protected void initializeNodeData(){
		node_data = new ArrayList<HOVNodeData>();
	}

	@Override
	protected void appendNodeData(DemandSet demand_set, ScenarioElement se) {
		node_data.add(new HOVNodeData(demand_set, (Node) se.getReference()));
	}

	@Override
	protected void update() throws BeatsException {

		for(int n=0;n<node_data.size();n++){
			HOVNodeData nd = node_data.get(n);

			// update node informatio
			nd.update_info();

			// OR->FR => beta = 0;
			for(int i=0;i<nd.ind_or.size();i++)
				for(int j=0;j<nd.ind_fr.size();j++)
					if(getMyScenario().get.currentTimeInSeconds()%dt_log==0){
						DebugLogger.write(logger_id, String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
                                getMyScenario().get.currentTimeInSeconds(),
                                nd.getId(),
                                nd.link_or.get(i).getId(),
                                nd.link_fr.get(j).getId(),
                                hov_vtype_id,
                                0d));

						((ActuatorCMS)actuators.get(n)).set_split(
								nd.link_or.get(i).getId(),
								nd.link_fr.get(j).getId(),
								hov_vtype_id,
								0d);
						}

			// OR->HOV => beta = 0
			for(int i=0;i<nd.ind_or.size();i++)
				if(getMyScenario().get.currentTimeInSeconds()%dt_log==0){
					DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
							getMyScenario().get.currentTimeInSeconds(),
							nd.getId(),
							nd.link_or.get(i).getId(),
							nd.link_hov_out.getId(),
							hov_vtype_id,
							0d));

					((ActuatorCMS)actuators.get(n)).set_split(
							nd.link_or.get(i).getId(),
							nd.link_hov_out.getId(),
							hov_vtype_id,
							0d);
					}

			// HOV->FR => beta = 0
			for(int j=0;j<nd.ind_fr.size();j++)
				if(getMyScenario().get.currentTimeInSeconds()%dt_log==0){
					DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
							getMyScenario().get.currentTimeInSeconds(),
							nd.getId(),
							nd.link_hov_in.getId(),
							nd.link_fr.get(j).getId(),
							hov_vtype_id,
							0d));

					((ActuatorCMS)actuators.get(n)).set_split(
							nd.link_hov_in.getId(),
							nd.link_fr.get(j).getId(),
							hov_vtype_id,
							0d);
					}

			// HOV->GP, apply beta if unset
			if(Double.isNaN(nd.get_SRP_hov_out_split())) {
				DebugLogger.write(logger_id, String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
						getMyScenario().get.currentTimeInSeconds(),
						nd.getId(),
						nd.link_hov_in.getId(),
						nd.link_gp_out.getId(),
						hov_vtype_id,
						nd.beta_hov_gp));

				((ActuatorCMS) actuators.get(n)).set_split(
						nd.link_hov_in.getId(),
						nd.link_gp_out.getId(),
						hov_vtype_id,
						nd.beta_hov_gp);
			}

			// GP->HOV, compute beta
			double beta = nd.gp_to_hov_flow_demand_ratio;
			beta = Math.min(beta,1d);

			if(getMyScenario().get.currentTimeInSeconds()%dt_log==0){
				DebugLogger.write(logger_id, String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
						getMyScenario().get.currentTimeInSeconds(),
						nd.getId(),
						nd.link_gp_in.getId(),
						nd.link_hov_out.getId(),
						hov_vtype_id,
						beta ));

				((ActuatorCMS)actuators.get(n)).set_split(
						nd.link_gp_in.getId() ,
						nd.link_hov_out.getId() ,
						hov_vtype_id,
						beta );
				}

			// GP->FR, adjust
			for(int j=0;j<nd.ind_fr.size();j++){
				double alpha = nd.beta_gp_frs[j];
				double r = (1d-beta);

				if(getMyScenario().get.currentTimeInSeconds()%dt_log==0)
					DebugLogger.write(logger_id,String.format("%f\t%d\t%d\t%d\t%d\t%f\n",
							getMyScenario().get.currentTimeInSeconds(),
							nd.getId(),
							nd.link_gp_in.getId() ,
							nd.link_fr.get(j).getId() ,
							hov_vtype_id,
							r*alpha ));

				((ActuatorCMS)actuators.get(n)).set_split(
						nd.link_gp_in.getId() ,
						nd.link_fr.get(j).getId() ,
						hov_vtype_id,
						r*alpha );
			}
		}
	}

	class HOVNodeData {

		Node myNode;
		protected int hov_vehtype_index;

		protected long id;
		protected int step_initial_abs;
		protected boolean isdone;
		protected List<BeatsTimeProfile> hov_downstream_flow;	// [veh] demand profile per vehicle type

		protected List<Link> link_hov;
		protected List<Link> link_not_hov;
		protected List<Link> link_gp;
		protected List<Link> link_not_gp;
		protected List<Link> link_or;
		protected List<Link> link_not_or;
		protected List<Link> link_fr;
		protected List<Link> link_not_fr;

		protected Link link_hov_in;
		protected Link link_gp_in;
		protected Link link_hov_out;
		protected Link link_gp_out;

		protected int ind_hov_in;
		protected int ind_gp_in;
		protected int ind_hov_out;
		protected int ind_gp_out;

		protected List<Integer> ind_hov;
		protected List<Integer> ind_not_hov;
		protected List<Integer> ind_gp;
		protected List<Integer> ind_not_gp;
		protected List<Integer> ind_or;
		protected List<Integer> ind_not_or;
		protected List<Integer> ind_fr;
		protected List<Integer> ind_not_fr;

		double total_non_onramp_demand;
		double [] known_non_offramp_demand;
		double [][] non_onramp_splits;
		double [] non_offramp_phi;
		double gp_to_hov_flow_demand_ratio;
		double [] non_offramp_xi;

		double [] hov_downstream_flow_veh;
		double total_hov_out_demand;
		double hov_to_gp_demand;
		double hov_to_hov_demand;
		double gp_to_hov_demand;
		double [] gp_to_fr_demand;
		double [] beta_gp_frs;
		double beta_hov_gp;

		protected List<Node> next_downstream_offramp_nodes;
		protected List<Link> next_downstream_offramps;
		protected List<Link> next_downstream_offramp_gps;


		public HOVNodeData(DemandSet demand_set, Node myNode) {
			this.id = myNode.getId();
			this.myNode = myNode;
			this.hov_vehtype_index = myScenario.get.vehicleTypeIndexForName("HOV");

			//  link references
			for(int i=0;i<myNode.getnIn();i++){
				Link link = myNode.getInput_link()[i];
				if(link.isHov()){
					link_hov_in = link;
					ind_hov_in = i;
				}
				else if(link.isFreeway()){
					link_gp_in = link;
					ind_gp_in = i;
				}
			}

			for(int j=0;j<myNode.getnOut();j++){
				Link link = myNode.getOutput_link()[j];
				if(link.isHov()) {
					link_hov_out = link;
					ind_hov_out = j;
				}
				else if(link.isFreeway()){
					link_gp_out = link;
					ind_gp_out = j;
				}
			}

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

			link_hov = new ArrayList<Link>();
			link_not_hov = new ArrayList<Link>();
			ind_hov = new ArrayList<Integer>();
			ind_not_hov = new ArrayList<Integer>();
			for(int j=0;j<myNode.getnOut();j++){
				Link link = myNode.getOutput_link()[j];
				if(link.isHov()){
					link_hov.add(link);
					ind_hov.add(j);
				}
				else{
					link_not_hov.add(link);
					ind_not_hov.add(j);
				}
			}

			link_gp = new ArrayList<Link>();
			link_not_gp = new ArrayList<Link>();
			ind_gp = new ArrayList<Integer>();
			ind_not_gp = new ArrayList<Integer>();
			for(int j=0;j<myNode.getnOut();j++){
				Link link = myNode.getOutput_link()[j];
				if(link.isFreeway()){
					link_gp.add(link);
					ind_gp.add(j);
				}
				else{
					link_not_gp.add(link);
					ind_not_gp.add(j);
				}
			}

			// find the demand profile for the downstream HOV link
			hov_downstream_flow = new ArrayList<BeatsTimeProfile>();
			List<Double> start_time = new ArrayList<Double>();
			edu.berkeley.path.beats.jaxb.DemandProfile dp = demand_set.get_demand_profile_for_link_id(link_hov_out.getId());
			if(dp==null)
				hov_downstream_flow.add(new BeatsTimeProfile("0",true));
			else{
				hov_downstream_flow.add(new BeatsTimeProfile(dp.getDemand().get(0).getContent(),true));
				start_time.add(Double.isInfinite(dp.getStartTime()) ? 0d : dp.getStartTime());
			}

			// find the next downstream offramps for plugging in splits if HOV out splits unset
			next_downstream_offramp_nodes = new ArrayList<Node>();
			next_downstream_offramp_gps = new ArrayList<Link>();
			next_downstream_offramps = new ArrayList<Link>();
			for(Link link : link_gp) {
				for(Link nextLink : link.getEnd_node().getOutput_link()) {
					if(nextLink.isOfframp()) {
						next_downstream_offramp_nodes.add(nextLink.getBegin_node());
						next_downstream_offramps.add(nextLink);
						next_downstream_offramp_gps.add(link);
						break;
					}
				}
				Link next_fr_from_this_gp = check_next_node_for_offramp(link);
				if(next_fr_from_this_gp!=null) {
					next_downstream_offramp_nodes.add(next_fr_from_this_gp.getBegin_node());
					next_downstream_offramps.add(next_fr_from_this_gp);
					try {
					next_downstream_offramp_gps.add( find_upstream_gp(next_fr_from_this_gp)); }
					catch (BeatsException ex) {
						System.err.println(ex.getMessage());
						next_downstream_offramp_nodes.remove( next_downstream_offramp_nodes.size() - 1);
						next_downstream_offramps.remove( next_downstream_offramps.size() - 1);
					}
					break;
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
			else {
				step_initial_abs = BeatsMath.round(start_time.get(0) / myScenario.get.simdtinseconds());
				isdone = false;
			}

		}

		public Link check_next_node_for_offramp(Link gp_link) { // TODO make generic, move to Link as findFirstDownstreamLinkOfType or something
			for( Link node_out_link : gp_link.getEnd_node().getOutput_link()) { // first check all output links to see if they are an offramp
				if(node_out_link.isOfframp())
					return node_out_link;
			}
			for( Link node_out_link : gp_link.getEnd_node().getOutput_link()) { // next check the downstream GP links' own downstream links
				if(node_out_link.isFreeway())
					return check_next_node_for_offramp(node_out_link);
			}
			return null; // no offramps or GP links found downstream of this GP link
		}

		public Link find_upstream_gp(Link offramp_link) throws BeatsException {
			for( Link upstream_link : offramp_link.getBegin_node().getInput_link()) {
				if(upstream_link.isFreeway())
						return upstream_link;
			}
			throw new BeatsException("The traverser broke when trying to find a GP link upstream of FR " + offramp_link.getId());
		}

		public void update_info(){

			// simple model - based on flow balance
			// assumes only one HOV link and one GP link
			// TODO add corrections for supply constraints if need for accuracy demands
			// TODO add consideration for multiple GP links and/or multiple HOV links

			int i,j;

			// if HOV outflow not set, find downstream FR split(s)
			beta_hov_gp = get_SRP_hov_out_split();
			if (Double.isNaN(beta_hov_gp)) {
				if(next_downstream_offramp_nodes.isEmpty()) {
					beta_hov_gp = 0d; }
				else {
					double [] downstream_fr_splits = new double[next_downstream_offramp_nodes.size()];
					for(int n=0;n<next_downstream_offramp_nodes.size();n++) {
						Node node = next_downstream_offramp_nodes.get(n);
						downstream_fr_splits[n] = node.getSplitRatioProfileValue(
								node.getInputLinkIndex(next_downstream_offramp_gps.get(n).getId()),
								node.getOutputLinkIndex(next_downstream_offramps.get(n).getId()),
								0); // temporary - uses only first vtype (assumed SOV)
					}
					GeometricMean geometricMean = new GeometricMean();
					beta_hov_gp = geometricMean.evaluate(downstream_fr_splits); // this HOV gate's SR is the mean of all routes' downstream FR SRs
				}
			}

			// retrieve HOV GP->FRs splitratio and demand
			beta_gp_frs = new double[link_fr.size()];
			gp_to_fr_demand = new double[link_fr.size()];
			for (j=0;j<link_fr.size();j++) {
				int jj = ind_fr.get(j);
				beta_gp_frs[j] = get_sr(ind_gp_in,jj,hov_vehtype_index);
				gp_to_fr_demand[j] = link_gp_in.get_out_demand_in_veh(0)[hov_vehtype_index] * beta_gp_frs[j];
			}

			double gp_to_gp_demand = link_gp_in.get_out_demand_in_veh(0)[hov_vehtype_index] * get_sr(ind_gp_in,ind_gp_out,hov_vehtype_index);

			// calculate directed demands of HOV lane
			hov_to_gp_demand = link_hov_in.get_out_demand_in_veh(0)[hov_vehtype_index] * beta_hov_gp;
			hov_to_hov_demand = link_hov_in.get_out_demand_in_veh(0)[hov_vehtype_index] * (1-beta_hov_gp);

			// hov_downstream_flow_veh
			hov_downstream_flow_veh = get_hov_flow_in_veh();
			double tot_hov_out_flow_veh = BeatsMath.sum(hov_downstream_flow_veh);

			double lacking_demand = tot_hov_out_flow_veh - hov_to_hov_demand;

			// gp_to_hov_flow_demand_ratio
			if(BeatsMath.lessorequalthan(lacking_demand, 0d))
			gp_to_hov_flow_demand_ratio = 0d;
			else if(BeatsMath.greaterthan(lacking_demand,gp_to_gp_demand))
				gp_to_hov_flow_demand_ratio = 1d;
			else
				gp_to_hov_flow_demand_ratio = lacking_demand / gp_to_gp_demand;

		}

		public double get_SRP_hov_out_split(){
			Link upstream_hov_link = null;
			Link downstream_gp_link = null;
			for(Link link : myNode.getInput_link()) {
				if(link.isHov()) {
					upstream_hov_link = link;
					break;
				}
			}
			for(Link link : myNode.getOutput_link()) {
				if(link.isFreeway()) {
					downstream_gp_link = link;
					break;
				}
			}
			if ((upstream_hov_link==null) || (downstream_gp_link==null))
					return 0d;
			if (myNode.getSplitRatioProfile().isConstant(
					upstream_hov_link.getId(), downstream_gp_link.getId(), hov_vehtype_index))
				return Double.NaN;
			return get_sr(myNode.getInputLinkIndex(upstream_hov_link.getId()),
					myNode.getOutputLinkIndex(downstream_gp_link.getId()),
					hov_vehtype_index);
		}

		public double get_sr(int ii,int jj,int kk){
			return myNode.getSplitRatioProfileValue(ii, jj, kk);
		}

		protected double [] get_hov_flow_in_veh(){

			double [] val = new double [link_hov.size()];
			int prof_sample_steps = 60;         ///// HACK!!!!

			if( !isdone && myScenario.get.clock().is_time_to_sample_abs(prof_sample_steps, step_initial_abs)){

				for(int i=0;i<link_hov.size();i++){

					BeatsTimeProfile profile = hov_downstream_flow.get(i);

					// REMOVE THESE
					int n = profile.getNumTime()-1;
					int step = myScenario.get.clock().sample_index_abs(prof_sample_steps,step_initial_abs);

					// demand is zero before step_initial_abs
					if(myScenario.get.clock().getAbsoluteTimeStep()< step_initial_abs)
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
				return this.hov_downstream_flow_veh;
		}

		protected long getId(){
			return id;
		}


	}

}
