package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public class LinkBehaviorCTM extends LinkBehavior {

    protected double [][] density;  // [veh] numEnsemble x numVehTypes

    public LinkBehaviorCTM(Link link){
        super(link);
    }

    /////////////////////////////////////////////////////////////////////
    // LinkBehaviorInterface
    /////////////////////////////////////////////////////////////////////

    // UPDATE

    @Override
    public void update_state(double [][] inflow,double [][] outflow){
        int e,j;
        for(e=0;e<myScenario.getNumEnsemble();e++)
            for(j=0;j<myScenario.getNumVehicleTypes();j++)
                density[e][j] += inflow[e][j] - outflow[e][j];
    }

//    @Override
//    public void update_total_space_supply(){
//        double totaldensity;
//        FundamentalDiagram FD;
//        for(int e=0;e<myScenario.getNumEnsemble();e++){
//            FD = myLink.currentFD(e);
//            totaldensity = myLink.getTotalDensityInVeh(e);
//            total_space_supply[e] = FD.getWNormalized()*(FD._getDensityJamInVeh() - totaldensity);
//            total_space_supply[e] = Math.min(total_space_supply[e],FD._getCapacityInVeh());
//
//            // flow uncertainty model
//            if(myScenario.isHas_flow_unceratinty()){
//                double delta_flow=0.0;
//                double std_dev_flow = myScenario.getStd_dev_flow();
//                switch(myScenario.getUncertaintyModel()){
//                    case uniform:
//                        delta_flow = BeatsMath.sampleZeroMeanUniform(std_dev_flow);
//                        break;
//
//                    case gaussian:
//                        delta_flow = BeatsMath.sampleZeroMeanGaussian(std_dev_flow);
//                        break;
//                }
//                total_space_supply[e] = Math.max( 0d , total_space_supply[e] + delta_flow );
//                total_space_supply[e] = Math.min( total_space_supply[e] , FD._getDensityJamInVeh() - totaldensity);
//            }
//        }
//    }

    // GET / SET / RESET DENSITY


    @Override
    public double get_density_in_veh(int ensemble_index, int vehicletype_index) throws IndexOutOfBoundsException {
        return density[ensemble_index][vehicletype_index];
    }

    @Override
    public boolean set_density_in_veh(int e,double [] d){
        if(density==null)
            return false;
        if(e<0 || e>=density.length)
            return false;
        if(d.length!=myScenario.getNumVehicleTypes())
            return false;
        if(!BeatsMath.all_non_negative(d))
            return false;
        for(int v=0;v<d.length;v++)
            density[e][v] = d[v];
        return true;
    }

    @Override
    public void reset_density(){
        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();
        density = BeatsMath.zeros(n1,n2);
    }

    // COMPUTE

    @Override
    public double compute_speed_in_mps(int ensemble){
        try{
            if(myScenario.getClock().getRelativeTimeStep()==0)
                return Double.NaN;
            double totaldensity = BeatsMath.sum(density[ensemble]);
            double speed = BeatsMath.greaterthan(totaldensity,0d) ?
                            BeatsMath.sum(myLink.outflow[ensemble])/totaldensity :
                            myLink.currentFD(ensemble).getVfNormalized();
            return speed * myLink._length / myScenario.getSimdtinseconds();
        } catch(Exception e){
            return Double.NaN;
        }
    }

    @Override
    public double compute_delay_in_veh(int ensemble, int vt_index){
        double n = get_density_in_veh(ensemble, vt_index);
        double f = myLink.getOutflowInVeh(ensemble, vt_index);
        double vf = myLink.getNormalizedVf(ensemble);
        return Math.max(0d,vf*n-f);
    }

}
