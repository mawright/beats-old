package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public interface LinkBehaviorInterface {

    public void updateOutflowDemand(double external_max_speed,double external_max_flow);

    public void updateSpaceSupply();

    public void overrideDensityWithVeh(double[] x,int ensemble);

    public double computeSpeedInMPS(int ensemble);

    public boolean set_density(double [] d);

    public double[] getDensityInVeh(int ensemble);

    public double getDensityInVeh(int ensemble,int vehicletype);

    public double getTotalDensityInVeh(int ensemble);

    public void update_state(double [][] inflow,double [][] outflow);

    public void initialize_density(double [] initial_density);

    public double computeTotalDelayInVeh(int ensemble);

    public double computeDelayInVeh(int ensemble,int vt_index);
}
