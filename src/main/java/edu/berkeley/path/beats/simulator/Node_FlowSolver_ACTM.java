package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/24/14.
 */
public class Node_FlowSolver_ACTM  extends Node_FlowSolver {

    // used in update()
    protected double [] outDemandKnown;	// [nOut]
    protected double [] dsratio;			// [nOut]
    protected boolean [][] iscontributor;	// [nIn][nOut]

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public Node_FlowSolver_ACTM(Node myNode) {
        super(myNode);
        System.out.println("Created ACTM node behavior;");
    }

    /////////////////////////////////////////////////////////////////////
    // implementation
    /////////////////////////////////////////////////////////////////////

    @Override
    protected void reset() {
        iscontributor = new boolean[myNode.nIn][myNode.nOut];
        dsratio 		= new double[myNode.nOut];
        outDemandKnown 	= new double[myNode.nOut];
    }

    @Override
    protected IOFlow computeLinkFlows(final Double3DMatrix sr,final int e){

        int i,j,k;
        int nIn = myNode.nIn;
        int nOut = myNode.nOut;
        int numVehicleTypes = myNode.myNetwork.getMyScenario().getNumVehicleTypes();

        double [][] demand = myNode.node_behavior.getDemand(e);
        double [] supply = myNode.node_behavior.getAvailableSupply(e);

        // input i contributes to output j .............................
        for(i=0;i<sr.getnIn();i++)
            for(j=0;j<sr.getnOut();j++)
                iscontributor[i][j] = sr.getSumOverTypes(i,j)>0;

        double [] applyratio = new double[nIn];

        for(i=0;i<nIn;i++)
            applyratio[i] = Double.NEGATIVE_INFINITY;

        for(j=0;j<nOut;j++){

            // re-compute known output demands .........................
            outDemandKnown[j] = 0d;
            for(i=0;i<nIn;i++)
                for(k=0;k<numVehicleTypes;k++)
                    outDemandKnown[j] += demand[i][k]*sr.get(i,j,k);

            // compute and sort output demand/supply ratio .............
            if(BeatsMath.greaterthan(supply[j],0d))
                dsratio[j] = Math.max( outDemandKnown[j] / supply[j] , 1d );
            else
                dsratio[j] = Double.POSITIVE_INFINITY;

            // reflect ratios back on inputs
            for(i=0;i<nIn;i++)
                if(iscontributor[i][j])
                    applyratio[i] = Math.max(dsratio[j],applyratio[i]);

        }


        IOFlow ioflow = new IOFlow(nIn,nOut,numVehicleTypes);

        // scale down input demands
        for(i=0;i<nIn;i++){
            for(k=0;k<numVehicleTypes;k++){
                ioflow.setIn(i,k , demand[i][k] / applyratio[i] );
            }
        }

        // compute out flows ...........................................
        for(j=0;j<nOut;j++){
            for(k=0;k<numVehicleTypes;k++){
                double val = 0d;
                for(i=0;i<nIn;i++)
                    val += ioflow.getIn(i,k)*sr.get(i,j,k);
                ioflow.setOut(j,k,val);
            }
        }

        return ioflow;
    }

    /////////////////////////////////////////////////////////////////////
    // protected interface
    /////////////////////////////////////////////////////////////////////

    protected Double3DMatrix resolveUnassignedSplits(final Double3DMatrix splitratio,final SupplyDemand demand_supply){
        return null;
    }
 }