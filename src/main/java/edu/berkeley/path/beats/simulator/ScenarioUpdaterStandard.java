package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/26/14.
 */
public class ScenarioUpdaterStandard extends ScenarioUpdaterAbstract {


    public ScenarioUpdaterStandard(Scenario scenario,String nodeflowsolver_name,String nodesrsolver_name){
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

        // update signals ...............................
        // NOTE: ensembles have not been implemented for signals. They do not apply
        // to pretimed control, but would make a differnece for feedback control.
//		if(signalSet!=null)
//			for(edu.berkeley.path.beats.jaxb.ActuatorSignal signal : signalSet.getSignal())
//				((ActuatorSignal)signal).update();

        // update controllers
        scenario.controllerset.update();

        // update and deploy actuators
        scenario.actuatorset.deploy(scenario.getCurrentTimeInSeconds());

        // update events
        scenario.eventset.update();

        // update the network state......................
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork()){
            Network net = (Network) network;
            net.update_supply_demand();
            net.update_flow();
            net.update_density();
        }

        scenario.cumulatives.update();

        if(scenario.perf_calc!=null)
            scenario.perf_calc.update();

        // advance the clock
        scenario.clock.advance();

    }

}
