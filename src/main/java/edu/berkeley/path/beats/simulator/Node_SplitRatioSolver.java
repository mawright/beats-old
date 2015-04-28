package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.Double3DMatrix;

public abstract class Node_SplitRatioSolver {

	protected Node myNode;

	protected abstract Double3DMatrix computeAppliedSplitRatio(final Double3DMatrix splitratio_selected,final int ensemble_index);
    protected abstract void reset();
    protected abstract void validate();
    
	public Node_SplitRatioSolver(Node myNode) {
		super();
		this.myNode = myNode;
	}

}
