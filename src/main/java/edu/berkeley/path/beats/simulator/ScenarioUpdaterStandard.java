package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.BeatsException;

/**
 * Created by gomes on 10/26/14.
 */
public class ScenarioUpdaterStandard extends ScenarioUpdaterAbstract {


    public ScenarioUpdaterStandard(Scenario scenario,String nodeflowsolver_name,String nodesrsolver_name){
        super(scenario,nodeflowsolver_name,nodesrsolver_name);
    }

    @Override
    public void update() throws BeatsException {

        update_profiles();

        update_sensors_control_events();

        // update the network state......................
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork()){
            Network net = (Network) network;
            update_supply_demand(net);
            update_flow(net);
            update_density(net);
        }

        update_cumalitives_clock();
    }

}
