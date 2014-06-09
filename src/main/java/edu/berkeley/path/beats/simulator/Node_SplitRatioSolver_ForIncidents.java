package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.Node_FlowSolver.SupplyDemand;

public class Node_SplitRatioSolver_ForIncidents extends Node_SplitRatioSolver {
	
	/* Fields */
	private double treshold = 33.4544 ;//TODO Remove hardcoded value
    private double scalingFactor = 0.0004; //TODO Remove hardcoded value
	
	/* Constructor */
	public Node_SplitRatioSolver_ForIncidents(Node myNode) {
		super(myNode);
		// TODO Auto-generated constructor stub
	}

	/* Adjustment of split ratio */
	@Override
	protected Double3DMatrix computeAppliedSplitRatio(Double3DMatrix splitratio_selected, SupplyDemand demand_supply) {
		// TODO Auto-generated method stub
		return null;
	}

	/* Reset function */
	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
