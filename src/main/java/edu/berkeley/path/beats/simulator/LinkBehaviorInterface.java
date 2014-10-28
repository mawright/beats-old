package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public interface LinkBehaviorInterface {

    // UPDATE

    // inflow, outflow are in veh units and index with [ensemble][vehicle_type]
    public void update_state(double [][] inflow,double [][] outflow);

    // typically min(v*n,F,control)
    public void update_outflow_demand(double external_max_speed, double external_max_flow);

    // typically simply njam-n
    public void update_total_space_supply();

    // GET / SET / RESET DENSITY

    public double get_density_in_veh(int ensemble_index, int vehicletype_index) throws IndexOutOfBoundsException;

    public boolean set_density_in_veh(int ensemble,double [] d);

    public void reset_density();

    // COMPUTE

    public double compute_speed_in_mps(int ensemble);

    public double compute_delay_in_veh(int ensemble, int vt_index);

}
