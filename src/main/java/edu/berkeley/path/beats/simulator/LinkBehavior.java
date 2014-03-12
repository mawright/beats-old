package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public class LinkBehavior {

    protected Link myLink;
    protected Scenario myScenario;

    protected double [][] density;    				    // [veh]	numEnsemble x numVehTypes

    // input to node model
    protected double [] spaceSupply;        			// [veh]	numEnsemble
    protected double [][] outflowDemand;   			    // [veh] 	numEnsemble x numVehTypes

    // in/out flows (from node model or demand profiles)
    protected double [][] inflow;    					// [veh]	numEnsemble x numVehTypes
    protected double [][] outflow;    				    // [veh]	numEnsemble x numVehTypes


    public LinkBehavior(Link link){
        this.myLink = link;
        this.myScenario = myLink.myNetwork.getMyScenario();

    }

    protected void reset(double [] initial_density) {

        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();

        density         = BeatsMath.zeros(n1,n2);
        inflow 	        = BeatsMath.zeros(n1,n2);
        outflow 		= BeatsMath.zeros(n1,n2);
        outflowDemand 	= BeatsMath.zeros(n1,n2);
        spaceSupply 	= BeatsMath.zeros(n1);

        // copy initial density to density
        int e,v;
        for(e=0;e<n1;e++)
            for(v=0;v<n2;v++)
                density[e][v] = initial_density[v];
    }

    protected void updateOutflowDemand(double external_max_speed,double external_max_flow){

        int numVehicleTypes = myScenario.getNumVehicleTypes();

        double totaldensity;
        double totaloutflow;
        FundamentalDiagram FD;

        for(int e=0;e<myScenario.getNumEnsemble();e++){

            FD = myLink.currentFD(e);

            totaldensity = BeatsMath.sum(density[e]);

            // case empty link
            if( BeatsMath.lessorequalthan(totaldensity,0d) ){
                outflowDemand[e] =  BeatsMath.zeros(numVehicleTypes);
                continue;
            }

            // compute total flow leaving the link in the absence of flow control
            if( totaldensity < FD.getDensityCriticalInVeh() ){
                totaloutflow = totaldensity * Math.min(FD.getVfNormalized(),external_max_speed);
            }
            else{
                totaloutflow = Math.max(FD._getCapacityInVeh()-FD._getCapacityDropInVeh(),0d);
                totaloutflow = Math.min(totaloutflow,external_max_speed*FD.getDensityCriticalInVeh());
            }

            // capacity profile
            if(myLink.myCapacityProfile!=null)
                totaloutflow = Math.min( totaloutflow , myLink.myCapacityProfile.getCurrentValue() );

            // flow controller
            totaloutflow = Math.min( totaloutflow , external_max_flow );

            // flow uncertainty model
            if(myScenario.isHas_flow_unceratinty()){

                double delta_flow=0.0;
                double std_dev_flow = myScenario.getStd_dev_flow();

                switch(myScenario.getUncertaintyModel()){
                    case uniform:
                        delta_flow = BeatsMath.sampleZeroMeanUniform(std_dev_flow);
                        break;

                    case gaussian:
                        delta_flow = BeatsMath.sampleZeroMeanGaussian(std_dev_flow);
                        break;
                }

                totaloutflow = Math.max( 0d , totaloutflow + delta_flow );
                totaloutflow = Math.min( totaloutflow , totaldensity );
            }

            // split among types
            outflowDemand[e] = BeatsMath.times(density[e],totaloutflow/totaldensity);
        }

        return;
    }

    protected void updateSpaceSupply(){
        double totaldensity;
        FundamentalDiagram FD;
        for(int e=0;e<myScenario.getNumEnsemble();e++){
            FD = myLink.currentFD(e);
            totaldensity = BeatsMath.sum(density[e]);
            spaceSupply[e] = FD.getWNormalized()*(FD._getDensityJamInVeh() - totaldensity);
            spaceSupply[e] = Math.min(spaceSupply[e],FD._getCapacityInVeh());

            // flow uncertainty model
            if(myScenario.isHas_flow_unceratinty()){
                double delta_flow=0.0;
                double std_dev_flow = myScenario.getStd_dev_flow();
                switch(myScenario.getUncertaintyModel()){
                    case uniform:
                        delta_flow = BeatsMath.sampleZeroMeanUniform(std_dev_flow);
                        break;

                    case gaussian:
                        delta_flow = BeatsMath.sampleZeroMeanGaussian(std_dev_flow);
                        break;
                }
                spaceSupply[e] = Math.max( 0d , spaceSupply[e] + delta_flow );
                spaceSupply[e] = Math.min( spaceSupply[e] , FD._getDensityJamInVeh() - totaldensity);
            }
        }
    }

    protected void update() {

        int e,j;

        if(myLink.issink)
            outflow = outflowDemand;

        if(myLink.issource && myLink.myDemandProfile!=null)
            for(e=0;e<myScenario.getNumEnsemble();e++)
                inflow[e] = myLink.myDemandProfile.getCurrentValue();

        for(e=0;e<myScenario.getNumEnsemble();e++)
            for(j=0;j<myScenario.getNumVehicleTypes();j++)
                density[e][j] += inflow[e][j] - outflow[e][j];

    }

    protected void overrideDensityWithVeh(double[] x,int ensemble){
        if(ensemble<0 || ensemble>=density.length)
            return;
        if(x.length!=density[0].length)
            return;

        int i;
        for(i=0;i<x.length;i++)
            if(x[i]<0)
                return;
        for(i=0;i<x.length;i++)
            density[ensemble][i] = x[i];
    }

    public double computeSpeedInMPS(int ensemble){
        try{
            if(myScenario.getClock().getRelativeTimeStep()==0)
                return Double.NaN;

            double totaldensity = BeatsMath.sum(density[ensemble]);
            double speed;
            if( BeatsMath.greaterthan(totaldensity,0d) )
                speed = BeatsMath.sum(outflow[ensemble])/totaldensity;
            else
                speed = myLink.currentFD(ensemble).getVfNormalized();
            return speed * myLink._length / myScenario.getSimdtinseconds();
        } catch(Exception e){
            return Double.NaN;
        }
    }

    // override density ..................................................
    protected boolean set_density(double [] d){
        if(myScenario.getNumVehicleTypes()!=1)
            return false;
        if(density==null)
            return false; //density = new double[d.length][1];
        if(density.length!=d.length)
            return false;
        for(int e=0;e<d.length;e++)
            density[e][0] = d[e];
        return true;
    }

    protected double[] getDensityInVeh(int ensemble) {
        try{
            return density[ensemble].clone();
        } catch(Exception e){
            return null;
        }
    }

    protected double getDensityInVeh(int ensemble,int vehicletype) {
        try{
            return density[ensemble][vehicletype];
        } catch(Exception e){
            return Double.NaN;
        }
    }

    protected double getTotalDensityInVeh(int ensemble) {
        try{
            if(density!=null)
                return BeatsMath.sum(density[ensemble]);
            return 0d;
        } catch(Exception e){
            return Double.NaN;
        }
    }

    protected double getTotalDensityInVPMeter(int ensemble) {
        return getTotalDensityInVeh(ensemble)/myLink._length;
    }


    protected double computeTotalDelayInVeh(int ensemble){
        double n = getTotalDensityInVeh(ensemble);
        double f = getTotalOutflowInVeh(ensemble);
        double vf = myLink.getNormalizedVf(ensemble);
        return Math.max(0d,vf*n-f);
    }

    protected double computeDelayInVeh(int ensemble,int vt_index){
        double n = getDensityInVeh(ensemble, vt_index);
        double f = getOutflowInVeh(ensemble, vt_index);
        double vf = myLink.getNormalizedVf(ensemble);
        return Math.max(0d,vf*n-f);
    }


    /////////////////////////////////////////////////////////////////////
    // supply and demand calculation
    /////////////////////////////////////////////////////////////////////

    protected double[] get_out_demand_in_veh(int ensemble) {
        return outflowDemand[ensemble];
    }

    protected double get_space_supply_in_veh(int ensemble) {
        return spaceSupply[ensemble];
    }

    /////////////////////////////////////////////////////////////////////
    // interface for node model
    /////////////////////////////////////////////////////////////////////

    protected void setInflow(int ensemble,double[] inflow) {
        this.inflow[ensemble] = inflow;
    }

    protected void setOutflow(int ensemble,double[] outflow) {
        this.outflow[ensemble] = outflow;
    }

    protected double[] getOutflowInVeh(int ensemble) {
        try{
            return outflow[ensemble].clone();
        } catch(Exception e){
            return null;
        }
    }

    protected double getTotalOutflowInVeh(int ensemble) {
        try{
            return BeatsMath.sum(outflow[ensemble]);
        } catch(Exception e){
            return Double.NaN;
        }
    }

    protected double getInflowInVeh(int ensemble,int vehicletype) {
        try{
            return inflow[ensemble][vehicletype];
        } catch(Exception e){
            return Double.NaN;
        }
    }

    protected double[] getInflowInVeh(int ensemble) {
        try{
            return inflow[ensemble].clone();
        } catch(Exception e){
            return null;
        }
    }

    protected double getTotalInlowInVeh(int ensemble) {
        try{
            return BeatsMath.sum(inflow[ensemble]);
        } catch(Exception e){
            return Double.NaN;
        }
    }

    protected double getOutflowInVeh(int ensemble,int vehicletype) {
        try{
            return outflow[ensemble][vehicletype];
        } catch(Exception e){
            return Double.NaN;
        }
    }

}
