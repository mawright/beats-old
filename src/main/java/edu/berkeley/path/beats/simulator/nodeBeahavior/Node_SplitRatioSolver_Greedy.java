package edu.berkeley.path.beats.simulator.nodeBeahavior;

import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;

import java.util.ArrayList;

public class Node_SplitRatioSolver_Greedy extends Node_SplitRatioSolver {

    protected double [] outDemandKnown;	    // [nOut]
	protected double [] dsratio;			// [nOut]

	public Node_SplitRatioSolver_Greedy(Node myNode) {
		super(myNode);
	}

	@Override
    public void validate() {
		// TODO Auto-generated method stu
	}
	
	@Override
    public void reset() {
		dsratio 		= new double[myNode.nOut];
		outDemandKnown 	= new double[myNode.nOut];
	}
	
	@Override
    public Double [][][] computeAppliedSplitRatio(final Double [][][] splitratio_selected,final int e) {

    	int i,j,k;
    	int numunknown;	
    	double dsmax, dsmin;
		int nIn = myNode.nIn;
		int nOut = myNode.nOut;        
    	int numVehicleTypes = myNode.getMyNetwork().getMyScenario().get.numVehicleTypes();

        if(myNode.istrivialsplit)
            return BeatsMath.ones(nIn,nOut,numVehicleTypes);

        String [] vTypes = myNode.getMyNetwork().getMyScenario().get.vehicleTypeNames();
        Double [][][] splitratio_new = splitratio_selected.clone();

    	double remainingSplit;
    	double num;

        double [][] demand = myNode.node_behavior.getDemand(e);
        double [] supply = myNode.node_behavior.getAvailableSupply(e);

    	ArrayList<Integer> unknownind = new ArrayList<Integer>();		// [unknown splits]
    	ArrayList<Double> unknown_dsratio = new ArrayList<Double>();	// [unknown splits]	
    	ArrayList<Integer> minind_to_nOut= new ArrayList<Integer>();	// [min unknown splits]
    	ArrayList<Integer> minind_to_unknown= new ArrayList<Integer>();	// [min unknown splits]
    	ArrayList<Double> sendtoeach = new ArrayList<Double>();			// [min unknown splits]

        // compute known output demands ................................
        for(j=0;j<nOut;j++){
            outDemandKnown[j] = 0f;
            for(i=0;i<nIn;i++)
                for(k=0;k<numVehicleTypes;k++)
                    if(!Double.isNaN(splitratio_selected[i][j][k]))
                        outDemandKnown[j] += splitratio_selected[i][j][k] * demand[i][k];
        }

        // fill in unassigned split ratios .............................
        for(i=0;i<nIn;i++){
            for(k=0;k<numVehicleTypes;k++){

                // number of outputs with unknown split ratio
                numunknown = 0;
                for(j=0;j<nOut;j++)
                    if(Double.isNaN(splitratio_selected[i][j][k]))
                        numunknown++;

                if(numunknown==0)
                    continue;

                // compute and sort output demand/supply ratio .................
                for(j=0;j<nOut;j++)
                    dsratio[j] = outDemandKnown[j] / supply[j];

                double [] sr_new = new double[myNode.nOut];

                // initialize sr_new, save location of unknown entries, compute remaining split
                unknownind.clear();
                unknown_dsratio.clear();
                remainingSplit = 1f;
                for(j=0;j<nOut;j++){
                    Double sr = splitratio_selected[i][j][k];
                    if(sr.isNaN()){
                        sr_new[j] = 0f;
                        if(myNode.getOutput_link()[j].canVTypeEnter(vTypes[k])) {
                            unknownind.add(j);                        // index to unknown output
                            unknown_dsratio.add(dsratio[j]);        // dsratio for unknown output
                        }
                    }
                    else {
                        sr_new[j] = sr;
                        remainingSplit -= sr;
                    }
                }

                // distribute remaining split until there is none left or
                // all dsratios are equalized
                while(BeatsMath.greaterthan(remainingSplit, 0d)){

                    // find most and least "congested" destinations
                    dsmax = Double.NEGATIVE_INFINITY;
                    dsmin = Double.POSITIVE_INFINITY;
                    for(Double r : unknown_dsratio){
                        dsmax = Math.max(dsmax,r);
                        dsmin = Math.min(dsmin,r);
                    }

                    if(BeatsMath.equals(dsmax,dsmin))
                        break;

                    // indices of smallest dsratio
                    minind_to_nOut.clear();
                    minind_to_unknown.clear();
                    sendtoeach.clear();		// flow needed to bring each dsmin up to dsmax
                    double sumsendtoeach = 0f;
                    for(int z=0;z<numunknown;z++)
                        if( BeatsMath.equals(unknown_dsratio.get(z),dsmin) ){
                            int index = unknownind.get(z);
                            minind_to_nOut.add(index);
                            minind_to_unknown.add(z);
                            num = dsmax*supply[index] - outDemandKnown[index];
                            sendtoeach.add(num);
                            sumsendtoeach += num;
                        }

                    // total that can be sent
                    double sendtotal = Math.min(demand[i][k]*remainingSplit , sumsendtoeach );

                    // scale down sendtoeach
                    // store split ratio
                    for(int z=0;z<minind_to_nOut.size();z++){
                        double send = sendtoeach.get(z)*sendtotal/sumsendtoeach;
                        double addsplit;
                        if(BeatsMath.equals(send,0d) || BeatsMath.equals(demand[i][k],0d))
                            addsplit = 1d;
                        else
                            addsplit = send/demand[i][k];
                        int ind_nOut = minind_to_nOut.get(z);
                        int ind_unknown = minind_to_unknown.get(z);

                        addsplit = Math.min(addsplit,1d-sr_new[ind_nOut]);
                        remainingSplit -= addsplit;
                        sr_new[ind_nOut] += addsplit;
                        outDemandKnown[ind_nOut] += addsplit*demand[i][k];
                        unknown_dsratio.set( ind_unknown , outDemandKnown[ind_nOut]/supply[ind_nOut] );
                    }
                }

                // distribute remaining splits proportionally to supplies
                if(BeatsMath.greaterthan(remainingSplit,0d)){
                    double totalsupply = 0f;
                    double splitforeach;
                    for(Integer jj : unknownind)
                        totalsupply += supply[jj];
                    for(Integer jj : unknownind){
                        splitforeach = remainingSplit*supply[jj]/totalsupply;
                        sr_new[jj] += splitforeach;
                        outDemandKnown[jj] += demand[i][k]*splitforeach;
                    }
                }

                // copy to SR
                for(j=0;j<nOut;j++)
                    splitratio_new[i][j][k] = sr_new[j];
            }
        }

    	return splitratio_new;

	}




}
