package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public class LinkBehaviorQueue extends LinkBehaviorCTM {

    public LinkBehaviorQueue(Link link){
        super(link);
    }

    /////////////////////////////////////////////////////////////////////
    // LinkBehaviorInterface
    /////////////////////////////////////////////////////////////////////

    @Override
    public double compute_speed_in_mps(int ensemble){
        try{
            if(myScenario.getClock().getRelativeTimeStep()==0)
                return Double.NaN;
            double totaldensity = BeatsMath.sum(density[ensemble]);
            double speed = BeatsMath.greaterthan(totaldensity,0d) ?
                           BeatsMath.sum(myLink.outflow[ensemble])/totaldensity : 1d;
            return speed * myLink._length / myScenario.getSimdtinseconds();
        } catch(Exception e){
            return Double.NaN;
        }
    }

    @Override
    public void update_outflow_demand(double external_max_speed, double external_max_flow){

        int numVehicleTypes = myScenario.getNumVehicleTypes();

        double totaldensity;
        double totaloutflow;
        FundamentalDiagram FD;

        for(int e=0;e<myScenario.getNumEnsemble();e++){

            FD = myLink.currentFD(e);

            totaldensity = getTotalDensityInVeh(e);

            // case empty link
            if( BeatsMath.lessorequalthan(totaldensity,0d) ){
                outflowDemand[e] =  BeatsMath.zeros(numVehicleTypes);
                continue;
            }

            // compute total flow leaving the link in the absence of flow control
            totaloutflow = Math.min(totaldensity*Math.min(1d,external_max_speed),FD._getCapacityInVeh());

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
            double alpha = totaloutflow/totaldensity;
            for(int j=0;j<myScenario.getNumVehicleTypes();j++)
                outflowDemand[e][j] = get_density_in_veh(e, j)*alpha;

        }

        return;
    }

    @Override
    public void update_space_supply(){
        double totaldensity;
        FundamentalDiagram FD;
        for(int e=0;e<myScenario.getNumEnsemble();e++){
            FD = myLink.currentFD(e);
            totaldensity = getTotalDensityInVeh(e);
            spaceSupply[e] = Math.min(FD._getDensityJamInVeh()-totaldensity,FD._getCapacityInVeh());

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

}
