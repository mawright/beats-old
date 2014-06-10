package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.Node_FlowSolver.SupplyDemand;

public class Node_SplitRatioSolver_ForIncidents extends Node_SplitRatioSolver {
	
	/* Fields */
	private double treshold = 33.4544 ;//TODO Remove hardcoded value
    private double scaling_factor = 0.0004; //TODO Remove hardcoded value
    private int fwy_id;
    private int off_ramp_id;
	
	/* Constructor */
	public Node_SplitRatioSolver_ForIncidents(Node myNode) {
		super(myNode);
		
		// Find diverging link
		for (int n = 0 ; n < myNode.output_link.length ; n++)
		{
			if (myNode.output_link[n].getLinkType().getId() == 1)
			{
				fwy_id = n;
			}
			else
			{
				off_ramp_id = n;
			}
		}		
	}

	/* Methods */
	// Method for adjust the split ratio
	@Override
	protected Double3DMatrix computeAppliedSplitRatio(Double3DMatrix splitratio_selected, SupplyDemand demand_supply) {
		
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	// Reset method
	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
