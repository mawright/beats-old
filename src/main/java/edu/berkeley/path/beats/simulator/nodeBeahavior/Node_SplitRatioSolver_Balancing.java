package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;

/**
 * Created by matt on 9/23/15.
 * Split ratio assignment algorithm described in Section 4 of Wright, Gomes, Horowitz and Kurzhanskiy,
 * "A new model for multi-commodity macroscopic modeling of complex traffic networks"
 */
public class Node_SplitRatioSolver_Balancing extends Node_SplitRatioSolver{

	public Node_SplitRatioSolver_Balancing(Node myNode) {
		super(myNode);
	}

	@Override
	public void validate() {

	}

	@Override
	public void reset() {

	}

	@Override
	public Double [][][] computeAppliedSplitRatio(final Double [][][] splitratio_selected,final int e) {

		return null;
	}

}
