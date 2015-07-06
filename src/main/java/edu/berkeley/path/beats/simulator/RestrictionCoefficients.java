package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.utils.Table;

import java.util.HashMap;

/**
 * Created by matt on 7/3/15.
 */
public class RestrictionCoefficients extends edu.berkeley.path.beats.jaxb.RestrictionCoefficients {

	private Node myNode;
	private HashMap<Link,Table> RestrictionMatrices;
	private boolean defaultFullRestriction = false;

	protected void populate(Node node) {
		this.myNode = node;

		int numIn = myNode.getnIn();
		RestrictionMatrices = new HashMap<Link, Table>(numIn);

		int i;
		// if only one mutual restriction matrix, it applies to all in-links
		if(getTable().size()==1){
			for(i=0;i<numIn;i++){
				Table matrixForAllLinks = new Table(getTable().get(0));
				RestrictionMatrices.put(myNode.getInput_link()[i],matrixForAllLinks);
			}
		}
		// if no mutual restriction matrices specified, default to behavior of strict FIFO
		// (all coefficients are equal to 1)
		else if(getTable().size()==0) {
			defaultFullRestriction = true;
		}
		else {
			Long inputLinkId;
			for(i=0;i<numIn;i++){
				inputLinkId = Long.parseLong(getTable().get(i).getName());
				try {
					RestrictionMatrices.put(
							myNode.getInput_link()[myNode.getInputLinkIndex(inputLinkId)],
							new Table(getTable().get(i)));
				}
				catch (ArrayIndexOutOfBoundsException ex){
					BeatsErrorLog.addWarning("Node with ID=" + myNode.getId() +
							" has a restriction matrix for input link ID=" + inputLinkId +
					" but no matching input link. Defaulting to strict FIFO for this node.");
					RestrictionMatrices.clear();
					defaultFullRestriction = true;
				}
			}
		}

	}

	protected void validate() {
		if(defaultFullRestriction)
				return;

		for(Table restMat: RestrictionMatrices.values()){

		}

	}


}
