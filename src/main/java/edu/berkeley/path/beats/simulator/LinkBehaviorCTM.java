package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public class LinkBehaviorCTM {

    protected Link myLink;
    protected Scenario myScenario;
    protected double [][] density;                  // [veh] numEnsemble x numVehTypes
    protected double [] total_space_supply;         // [veh]	numEnsemble (typically njam-n)
    protected double [] available_space_supply;     // [veh]	numEnsemble (typically min(w*(njam-n),F))
    protected double [][] flow_demand;              // [veh] 	numEnsemble x numVehTypes (typically min(vn,F,c))

    public LinkBehaviorCTM(Link link){
        this.myLink = link;
        this.myScenario = myLink.myScenario;
    }

    protected void reset(double [] initial_density) {
        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();
        flow_demand = BeatsMath.zeros(n1,n2);
        total_space_supply = BeatsMath.zeros(n1);
        available_space_supply = BeatsMath.zeros(n1);
        reset_density();
        for(int e=0;e<n1;e++)
            set_density_in_veh(e,initial_density);
    }

    public void update_outflow_demand(double external_max_speed, double external_max_flow){

        int numVehicleTypes = myScenario.getNumVehicleTypes();

        double totaldensity;
        double totaloutflow;
        FundamentalDiagram FD;

        for(int e=0;e<myScenario.getNumEnsemble();e++){

            FD = myLink.currentFD(e);

            totaldensity = myLink.getTotalDensityInVeh(e);

            // case empty link
            if( BeatsMath.lessorequalthan(totaldensity,0d) ){
                flow_demand[e] =  BeatsMath.zeros(numVehicleTypes);
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

            // flow uncertainty model (unless controller wants zero flow)
            if(myScenario.isHas_flow_unceratinty() && BeatsMath.greaterthan(external_max_flow,0d) ){

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
            if(myScenario.getNumVehicleTypes()==1)
                flow_demand[e][0] = totaloutflow;
            else{
                double alpha = totaloutflow/totaldensity;
                for(int j=0;j<myScenario.getNumVehicleTypes();j++)
                    flow_demand[e][j] = get_density_in_veh(e, j)*alpha;
            }

        }

        return;
    }

    public void update_total_space_supply(){
        for(int e=0;e<myScenario.getNumEnsemble();e++)
            total_space_supply[e] = myLink.currentFD(e)._getDensityJamInVeh() - myLink.getTotalDensityInVeh(e);
    }

    public void update_available_space_supply(){
        for(int e=0;e<myScenario.getNumEnsemble();e++){
            FundamentalDiagram FD = myLink.currentFD(e);
            available_space_supply[e] = Math.min( FD.getWNormalized()*total_space_supply[e] , FD._getCapacityInVeh() );

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
                available_space_supply[e] = Math.max( 0d , available_space_supply[e] + delta_flow );
                available_space_supply[e] = Math.min( available_space_supply[e] , total_space_supply[e] );
            }
        }
    }

    protected void reset_density(){
        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();
        density = BeatsMath.zeros(n1,n2);
    }

    // UPDATE

    public void update_state(double [][] inflow,double [][] outflow){
        int e,j;
        for(e=0;e<myScenario.getNumEnsemble();e++)
            for(j=0;j<myScenario.getNumVehicleTypes();j++)
                density[e][j] += inflow[e][j] - outflow[e][j];
    }

    // GET / SET / RESET DENSITY

    public double get_density_in_veh(int ensemble_index, int vehicletype_index) throws IndexOutOfBoundsException {
        return density[ensemble_index][vehicletype_index];
    }

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

    // COMPUTE

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

    public double compute_delay_in_veh(int ensemble, int vt_index){
        double n = get_density_in_veh(ensemble, vt_index);
        double f = myLink.getOutflowInVeh(ensemble, vt_index);
        double vf = myLink.getNormalizedVf(ensemble);
        return Math.max(0d,vf*n-f);
    }

}
