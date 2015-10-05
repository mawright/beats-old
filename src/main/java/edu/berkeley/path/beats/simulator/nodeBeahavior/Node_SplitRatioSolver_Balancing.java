package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 9/23/15.
 * Split ratio assignment algorithm described in Section 4 of Wright, Gomes, Horowitz and Kurzhanskiy,
 * "A new model for multi-commodity macroscopic modeling of complex traffic networks"
 */
public class Node_SplitRatioSolver_Balancing extends Node_SplitRatioSolver{

	protected Double [][][] computed_splitratio; //nIn x nOut x nVType
	protected int nVType;
	protected Double [][] demands;
	protected double [] inputPriorities;

	protected boolean [][][] splitKnown;
	protected Double [][] splitRemaining; // nIn x nVType

	protected List<Integer>[] U_j; // links i that wish to send to link j
	protected List<Integer>[][] V_ic; // links j to which (i,c) has undefined split ratios
	protected List<Integer> V; // links j to which split may still be assigned

	protected double[][] unallocated_demand;
	protected double[][][] oriented_demand;
	protected double[][] oriented_priority;

	private final double zeroThreshold = Double.MIN_VALUE * 2;

	public Node_SplitRatioSolver_Balancing(Node myNode) {
		super(myNode);
	}

	@Override
	public void validate() {

	}

	@Override
	public void reset() {
		nVType = myNode.getMyNetwork().getMyScenario().get.numVehicleTypes();
		unallocated_demand = new double[myNode.nIn][nVType];
		oriented_demand = new double[myNode.nIn][myNode.nOut][nVType];
		oriented_priority = new double[myNode.nIn][myNode.nOut];
	}

	@Override
	public Double [][][] computeAppliedSplitRatio(final Double [][][] splitratio_selected,final int e) {

		// get link information
		inputPriorities = myNode.getInputLinkPriorities(e);
		for(int i=0;i<myNode.getnIn();i++){
			demands[i] = myNode.getInput_link()[i].get_out_demand_in_veh(e);
		}

		initializeAssignedSplits(splitratio_selected);
		assignZeroDemandSplits();
		assignZeroRemainingSplits();

		initializeSets();
		regularizePriorities();

		while(!allSplitsSolved()) {
			calculateRemainingDemand();
			calculateOrientedDemand();
			calculateOrientedPriorities(splitratio_selected);
		}

		return computed_splitratio;
	}

	private void initializeAssignedSplits(final Double[][][] input_splitratio) {
		for(int i=0;i<myNode.nIn;i++) {
			for(int c=0;c<nVType;c++) {
				// initialize counter of how much split remains to be assigned assignment
				splitRemaining[i][c] = 1d;

				for(int j=0;j<myNode.nOut;j++) {

					if (Double.isNaN(input_splitratio[i][j][c])) {
						splitKnown[i][j][c] = false;
					}
					else {
						splitKnown[i][j][c] = true;
						splitRemaining[i][c] = splitRemaining[i][c] - input_splitratio[i][j][c];
						computed_splitratio[i][j][c] = input_splitratio[i][j][c];
					}
				}
			}
		}
	}

	private void assignZeroDemandSplits() {
		for (int i=0;i<myNode.nIn;i++){
			for (int c=0;c<nVType;c++){
				if (demands[i][c] < zeroThreshold) { // splits may be assigned arbitrarily - assign 1 to first j
					computed_splitratio[i][0][c] = 1d;
					splitRemaining[i][c] = 0d;
					splitKnown[i][0][c] = true;

					for(int j=1;j<myNode.nOut;j++){
						computed_splitratio[i][j][c] = 0d;
						splitKnown[i][j][c] = true;
					}
				}
			}
		}
	}

	private void assignZeroRemainingSplits() {
		int i,j,c;
		for(i=0;i<myNode.nIn;i++){
			for(c=0;c<nVType;c++){
				if (splitRemaining[i][c] < zeroThreshold) {
					for(j=0;j<myNode.nOut;j++) {
						splitKnown[i][j][c] = true;
						computed_splitratio[i][j][c] = 0d;
					}
				}
			}
		}
	}

	private void initializeSets() {
		int i,j,c;

		V = new ArrayList<Integer>(myNode.nOut);

		for(j=0;j<myNode.nOut;j++){
			U_j[j] = new ArrayList<Integer>(myNode.nIn);

			outerloop1:
			for(i=0;i<myNode.nIn;i++){
				for(c=0;c<nVType;c++){
					if (!splitKnown[i][j][c]) {
						U_j[j].add(i);
						break outerloop1;
					}
				}
			}
			if(!U_j[j].isEmpty())
				V.add(j);
		}

		for(i=0;i<myNode.nIn;i++){
			for(c=0;c<nVType;c++){
				V_ic[i][c] = new ArrayList<Integer>(myNode.nOut);
				for(j=0;j<myNode.nOut;j++){
					if (U_j[j].contains(i))
						V_ic[i][c].add(j);
				}
				if (V_ic[i][c].size()==1) {
					j = V_ic[i][c].get(0);
					splitKnown[i][j][c] = true;
					computed_splitratio[i][j][c] = splitRemaining[i][c];
					splitRemaining[i][c] = 0d;
					V_ic[i][c].clear();
				}
			}
		}
	}

	private void regularizePriorities() {
		int numZeroPriority = 0;
		int i;
		for (i = 0; i < myNode.nIn; i++) {
			if (inputPriorities[i] <  zeroThreshold)
				numZeroPriority++;
		}
		if (numZeroPriority==0)
			return;

		for (i = 0; i <myNode.nIn; i++) {
			inputPriorities[i] = inputPriorities[i] * (myNode.nIn - numZeroPriority) / numZeroPriority
					+ numZeroPriority / (Math.pow(myNode.nIn,2));
		}
	}

	private boolean allSplitsSolved() {
		return V.isEmpty();
	}

	private void calculateRemainingDemand() {
		for (int i = 0; i < myNode.nIn; i++) {
			for (int c=0; c < nVType; c++) {
				unallocated_demand[i][c] = splitRemaining[i][c] * demands[i][c];
			}
		}
	}

	private void calculateOrientedDemand() {
		for (int i = 0; i < myNode.nIn; i++) {
			for (int j=0; j<myNode.nOut; j++) {
				for (int c=0; c < nVType; c++) {
					oriented_demand[i][j][c] = computed_splitratio[i][j][c] * demands[i][c];
				}
			}
		}
	}

	private void calculateOrientedPriorities(final Double[][][] input_splitratio) {
		double[] gamma = new double[nVType];
		double[] numerator = new double[nVType];
		for (int i = 0; i<myNode.nIn; i++) {
			for( int j=0; j<myNode.nOut; j++) {
				for( int c=0; c<nVType; c++) {
					if(Double.isNaN(input_splitratio[i][j][c]))
						gamma[c] = input_splitratio[i][j][c];
					else
						gamma[c] = computed_splitratio[i][j][c] + splitRemaining[i][c] / V_ic[i][c].size();

					numerator[c] = gamma[c] * demands[i][c];
				}

				oriented_priority[i][j] = inputPriorities[i] * BeatsMath.sum(numerator) / BeatsMath.sum(demands[i]);
			}
		}
	}

	private void findLargestOrientedDSRatio() {
		
	}

	private ArrayList<Integer> makeTuple(int i, int j, int c) {
		ArrayList<Integer> output = new ArrayList<Integer>(3);
		output.add(0,i);
		output.add(1,j);
		output.add(2,c);
		return output;
	}

}
