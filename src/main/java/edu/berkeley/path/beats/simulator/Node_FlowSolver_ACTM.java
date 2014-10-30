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