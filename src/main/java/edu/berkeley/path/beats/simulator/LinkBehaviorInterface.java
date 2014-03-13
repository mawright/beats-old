package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public interface LinkBehaviorInterface {

    // UPDATE

    public void update_state(double [][] inflow,double [][] outflow);

    public void updateOutflowDemand(double external_max_speed,double external_max_flow);

    public void updateSpaceSupply();

    // GET

    public double[] getDensityInVeh(int ensemble);

    public double getDensityInVeh(int ensemble,int vehicletype);

    public double getTotalDensityInVeh(int ensemble);

    // COMPUTE

    public double computeSpeedInMPS(int ensemble);

    public double computeTotalDelayInVeh(int ensemble);

    public double computeDelayInVeh(int ensemble,int vt_index);

    // SET

    public void reset_density();

    public boolean overrideDensityWithVeh(double[] x,int ensemble);

    public boolean set_density_in_veh(int ensemble,double [] d);

//    public void initialize_density(double [] initial_density);

}
