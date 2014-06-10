package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.Node_FlowSolver.SupplyDemand;

public class Node_SplitRatioSolver_ForIncidents extends Node_SplitRatioSolver {
	
	/* Fields */
	private double treshold = 33.4544 ;//TODO Remove hardcoded value
    private double scaling_factor = 0.0004; //TODO Remove hardcoded value
    private int fwy_id = -1;
    private int off_ramp_id = -1;

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
			else if (myNode.output_link[n].getLinkType().getId() == 4 || myNode.output_link[n].getLinkType().getId() == 5)
			{
				off_ramp_id = n;
			}
		}		
	}
		
	/* Methods */
	// Method for adjust the split ratio
	@Override
	protected Double3DMatrix computeAppliedSplitRatio(Double3DMatrix splitratio_selected, SupplyDemand demand_supply,final int ensemble_index) {
		
		// sr_predict = sr_local_avg + K * max(density - thresh, 0)
		
		
		// TODO Auto-generated method stub
		return null;
	}

	// Reset method
	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}
	
	// Validation method
	@Override
	protected void validate() {
		// Validation of incoming links
		if(myNode.input_link.length != 1)
		{
			BeatsErrorLog.addError("Incorrect number of incomming links at node ID=" + myNode.getId() + " , total number of incomming links are " + myNode.input_link.length + " it must be 1.");
		}
		
		// Validation of outgoing links
		if(myNode.output_link.length != 2)
		{
			BeatsErrorLog.addError("Incorrect number of incomming links at node ID=" + myNode.getId() + " , total number of incomming links are " + myNode.output_link.length + " it must be 2.");
		}
		
		if(fwy_id == -1)
		{
			BeatsErrorLog.addError("Missing downstream link of type Freeway at node ID=" + myNode.getId() + " ,  it must be exactly one link downstream of type Freeway.");
		}
		
		if( off_ramp_id == -1)
		{
			BeatsErrorLog.addError("Missing diverging link of type Off-ramp/Interconnect at node ID=" + myNode.getId() + " ,  it must be exactly one diverging link of type Off-ramp or Interconnect.");
		}
	}

}
