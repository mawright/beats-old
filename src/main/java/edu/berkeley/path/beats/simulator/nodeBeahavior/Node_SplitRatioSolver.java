package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;

public abstract class Node_SplitRatioSolver {

    public Node myNode;

    public abstract Double [][][] computeAppliedSplitRatio(final Double [][][] splitratio_selected,final int ensemble_index);
    public abstract void reset();
    public abstract void validate();
    
	public Node_SplitRatioSolver(Node myNode) {
		super();
		this.myNode = myNode;
	}

}
