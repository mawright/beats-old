package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;

/**
 * Created by matt on 7/1/15.
 */
public class Node_FlowSolver_General extends Node_FlowSolver {

	// used in update()
	protected double [] outDemandKnown;	// [nOut]
	protected double [] dsratio;			// [nOut]
	protected boolean [][] iscontributor;	// [nIn][nOut]

	protected double [][] demands; // [nIn][nVType]
	protected double [][][] directed_demands; // [nIn][nOut][nVType]
	protected double [] priorities; // [nIn]
	protected double [][] directed_priorities; // [nIn][nOut]
	protected double [][][] restriction_coefficients; // [nIn][nOut][nOut]
	private boolean all_flows_solved;

	// constructor
	public Node_FlowSolver_General(Node myNode){
		super(myNode);
	}

	// implementation

	// TODO write restriction matrices parsers and getters
	// extend restrictionMatrices class
	// finish writing

	@Override
	public void reset() {
		iscontributor = new boolean[myNode.nIn][myNode.nOut];
		dsratio 		= new double[myNode.nOut];
		outDemandKnown 	= new double[myNode.nOut];
	}

	@Override
	public IOFlow computeLinkFlows(final Double [][][] sr, final int ensemble_index){

		int i,j,c; // input, output, commodity indices
		priorities = myNode.getInputLinkPriorities(ensemble_index);
		demands = myNode.node_behavior.getDemand(ensemble_index);

		//restriction_coefficients = myNode.getRestrictionCoefficients();

		all_flows_solved = false;

		while(!all_flows_solved){

		}

		return null;
	}

	private double [] solveMISO(Link outlink, int ensemble_index){

		double [] MISOflows = new double[myNode.nIn];
		double supply = outlink.get_available_space_supply_in_veh(ensemble_index);


		return MISOflows;
	}



}
