package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.utils.Double3DMatrix;

public abstract class Node_SplitRatioSolver {

    public Node myNode;

    public abstract Double3DMatrix computeAppliedSplitRatio(final Double3DMatrix splitratio_selected,final int ensemble_index);
    public abstract void reset();
    public abstract void validate();
    
	public Node_SplitRatioSolver(Node myNode) {
		super();
		this.myNode = myNode;
	}

}
