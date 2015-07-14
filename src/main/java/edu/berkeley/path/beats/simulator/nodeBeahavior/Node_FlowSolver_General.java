package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.RestrictionCoefficients;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by matt on 7/1/15.
 */
public class Node_FlowSolver_General extends Node_FlowSolver {

	// used in update()
	protected double [] outDemandKnown;	// [nOut]
	protected double [] dsratio;			// [nOut]
	protected boolean [][] iscontributor;	// [nIn][nOut]

	protected double [][] demands; // [nIn][nVType]
	protected double [] supplies; // [nOut]
	protected double [][][] directed_demands; // [nIn][nOut][nVType]
	protected double [] priorities; // [nIn]
	protected double [][] oriented_priorities; // [nIn][nOut]
	protected double [] reduction_factors; // [nOut]
	protected int min_reduction_index;
	protected ArrayList<Integer> freeflow_inlinks;
	protected HashMap< int[], Double > flows;
	protected IOFlow ioFlow;

	private RestrictionCoefficients restrictionCoefficients;

	private int c_max; // num of VTypes

	private boolean [] outlink_done; // [nOut], entry is true if outlink has all its inflows computed, false otherwise
	private boolean [] inlink_done; // [nIn], entry is true if inlink has all its outflows computed, false otherwise

	// constructor
	public Node_FlowSolver_General(Node myNode){
		super(myNode);
	}

	// implementation

	@Override
	public void reset() {
		iscontributor = new boolean[myNode.nIn][myNode.nOut];
		dsratio 		= new double[myNode.nOut];
		outDemandKnown 	= new double[myNode.nOut];

		directed_demands = new double[myNode.nIn][myNode.nOut][myNode.getMyNetwork().getMyScenario().get.numVehicleTypes()];
		reduction_factors = new double[myNode.nOut];
		c_max = myNode.getMyNetwork().getMyScenario().get.numVehicleTypes();
		freeflow_inlinks = new ArrayList<Integer>(myNode.nIn);
	}

	@Override
	public IOFlow computeLinkFlows(final Double [][][] sr, final int ensemble_index){

		int i,j,c; // input, output, commodity indices
		priorities = Arrays.copyOf(myNode.getInputLinkPriorities(ensemble_index), myNode.nIn);
		demands = Arrays.copyOf(myNode.node_behavior.getDemand(ensemble_index), myNode.nIn);
		supplies = Arrays.copyOf(myNode.node_behavior.getAvailableSupply(ensemble_index), myNode.nOut);

		restrictionCoefficients = myNode.getRestrictionCoefficients();

		// initialize directed demands
		for(i=0;i<sr.length;i++){
			for(j=0;j<sr[i].length;j++){
				for(c=0;c<sr[i][j].length;c++){
					directed_demands[i][j][c] = demands[i][c] * sr[i][j][c];
				}
			}
		}

		outlink_done = new boolean[myNode.getnOut()];
		inlink_done = new boolean[myNode.getnIn()];

		IOFlow ioflow = new IOFlow(myNode.getnIn(),myNode.getnOut(),c_max);

		determineUnsolvedMovements();

		while(!allFlowsSolved()){
			updatePriorities();
			computeOrientedPriorities();
			computeReductionFactors();
			determineFreeflowInlinks();
			pickFlows();
			setFlowsUpdateItems();
			determineUnsolvedMovements();
		}

		return ioflow;
	}

	private void determineUnsolvedMovements() { // determine membership of set V(k)
		int i,j;
		// input i contributes to output j .............................
		for(j=0;j<myNode.getnOut();j++){
			for(i=0;i<myNode.getnIn();i++){
				iscontributor[i][j] = BeatsMath.sum(directed_demands[i][j]) > 0;
			}
			for(i=0;i<myNode.getnIn();i++) {
				if(iscontributor[i][j]){
					inlink_done[i] = false;
					break;
				}
				inlink_done[i] = true;
			}
		}
		for(i=0;i<myNode.getnIn();i++) {
			for(j=0;j<myNode.getnOut();j++) {
				if(iscontributor[i][j]){
					outlink_done[j] = false;
					break;
				}
			}
			outlink_done[j] = true;
		}
	}

	private void updatePriorities() { // equation (5.4)
		int i,j;
		for(j=0;j<myNode.getnOut();j++) {
			if(!outlink_done[j]) {
				for(i=0;i<myNode.getnIn();i++) {
					if(iscontributor[i][j] && !inlink_done[i] && priorities[i] > 0)
						return; // if there are any unprocessed in-links with nonzero priority, do not adjust
				}
			}
		}
		int numUnsetInlinks = BeatsMath.count(inlink_done);
		for(i=0;i<myNode.getnIn();i++) {
			if(!inlink_done[i])
				priorities[i] = 1/numUnsetInlinks; // otherwise
		}
	}

	private void computeOrientedPriorities() { // equation (5.12)
		int i,j;
		double[] sum_over_c = new double[directed_demands[0].length];
//		double sum_over_c_and_j;
		for(i=0;i<directed_demands.length;i++){
			for(j=0;j<directed_demands[i].length;j++) {
				sum_over_c[j] = BeatsMath.sum(directed_demands[i][j]);
			}
//			sum_over_c_and_j = BeatsMath.sum(sum_over_c);
			for(j=0;j<directed_demands[i].length;j++) {
//				oriented_priorities[i][j] = priorities[i] * sum_over_c[j] / sum_over_c_and_j;
				oriented_priorities[i][j] = priorities[i] * sum_over_c[j] / BeatsMath.sum(demands[i]);
			}
		}
	}

	private void computeReductionFactors() { // compute factors a_j
		int i,j;
		double sum_over_i;
		for(j=0;j<myNode.nOut;j++){
			sum_over_i = 0d;
			for(i=0;i<myNode.nIn;i++){
				if(iscontributor[i][j])
					sum_over_i += oriented_priorities[i][j];
			}
			reduction_factors[j] = supplies[j] / sum_over_i;
			if(reduction_factors[j] <= reduction_factors[min_reduction_index])
				min_reduction_index = j;
		}

	}

	private void determineFreeflowInlinks() { // find members of set U-tilde(k)
		int i;
		for(i=0;i<myNode.nIn;i++){
			if( iscontributor[i][min_reduction_index] &&
					BeatsMath.sum(demands[i]) <= priorities[i] * reduction_factors[min_reduction_index] )
				freeflow_inlinks.add(i);
		}
	}

	private void pickFlows() { // set to IOFlow flows found in this iteration
		if(!freeflow_inlinks.isEmpty()){
			for(int i : freeflow_inlinks){
				for(int j=0;j<myNode.nOut;j++){
					for(int c=0;c<c_max;c++){
						int[] indexTriple = new int[3];
						indexTriple[0] = i; indexTriple[1] = j; indexTriple[2] = c;
						flows.put( indexTriple, directed_demands[i][j][c] );
					}
				}
			}
		}
		else {
			for(int i=0;i<myNode.nIn;i++) {
				for(int j=0;j<myNode.nOut;j++) {
					if(iscontributor[i][j] && iscontributor[i][min_reduction_index]) {
						double sum_over_c = BeatsMath.sum(directed_demands[i][j]);
						for(int c=0;c<c_max;c++) {
							int[] indexTriple = new int[3];
							indexTriple[0] = i;	indexTriple[1] = j;	indexTriple[2] = c;

							double flow = directed_demands[i][j][c] * oriented_priorities[i][j]
									* reduction_factors[min_reduction_index] / sum_over_c;
							flows.put(indexTriple, flow);
						}
					}
				}
			}
		}
	}

	private void setFlowsUpdateItems() { // update demand, supplies

	}

	private boolean allFlowsSolved() {
		for (boolean outlink_computed : outlink_done){
			if(!outlink_computed)
				return false;
		}
		return true;
	}



}
