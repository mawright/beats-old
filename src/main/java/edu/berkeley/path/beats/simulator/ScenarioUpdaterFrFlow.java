package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/26/14.
 */
public class ScenarioUpdaterFrFlow extends ScenarioUpdaterAbstract {

    public ScenarioUpdaterFrFlow(Scenario scenario,String nodeflowsolver_name,String nodesrsolver_name){
        super(scenario,nodeflowsolver_name,nodesrsolver_name);
    }

    @Override
    public void update() throws BeatsException {

        // sample profiles .............................
        if(scenario.getDownstreamBoundaryCapacitySet()!=null)
            ((CapacitySet)scenario.getDownstreamBoundaryCapacitySet()).update();

        if(scenario.getDemandSet()!=null)
            ((DemandSet)scenario.getDemandSet()).update();

        if(scenario.getSplitRatioSet()!=null)
            ((SplitRatioSet) scenario.getSplitRatioSet()).update();

        if(scenario.getFundamentalDiagramSet()!=null)
            for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdProfile : scenario.getFundamentalDiagramSet().getFundamentalDiagramProfile())
                ((FundamentalDiagramProfile) fdProfile).update(false);

        // update sensor readings .......................
        scenario.sensorset.update();

        // update supply/demand prior to controllers if run_mode==fw_fr_split_output
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork())
            update_supply_demand((Network) network);

        // update controllers
        scenario.controllerset.update();

        // update and deploy actuators
        scenario.actuatorset.deploy(scenario.getCurrentTimeInSeconds());

        // update events
        scenario.eventset.update();

        // update the network state......................
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork()){
            Network net = (Network) network;
            update_flow(net);
            update_density(net);
        }

        scenario.cumulatives.update();

        if(scenario.perf_calc!=null)
            scenario.perf_calc.update();

        // advance the clock
        scenario.clock.advance();
    }

}
