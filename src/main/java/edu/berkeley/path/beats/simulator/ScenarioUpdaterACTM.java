package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 10/26/14.
 */
public class ScenarioUpdaterACTM extends ScenarioUpdaterAbstract {

    private List<Link> onramp_links;
    private List<Link> not_onramp_links;

    public ScenarioUpdaterACTM(Scenario scenario){
        super(scenario,null,"A");
        System.out.println("ERROR!!! ACTM link and node behaviors not implemented");
    }

    @Override
    public void populate() {
        super.populate();
        // for actm, collect references to onramp links
        onramp_links = new ArrayList<Link>();
        not_onramp_links = new ArrayList<Link>();
        for (edu.berkeley.path.beats.jaxb.Link link : scenario.getNetworkSet().getNetwork().get(0).getLinkList().getLink())  {
            Link bLink = (Link)link;
            if(bLink.isOnramp())
                onramp_links.add(bLink);
            else
                not_onramp_links.add(bLink);
        }
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

        // update controllers
        scenario.controllerset.update();

        // update and deploy actuators
        scenario.actuatorset.deploy(scenario.getCurrentTimeInSeconds());

        // update events
        scenario.eventset.update();

        // first update onramps
        for(Link link : onramp_links ){
            link.updateOutflowDemand();
            link.updateSpaceSupply();
        }
//        for (edu.berkeley.path.beats.jaxb.Node node : net.getNodeList().getNode())
//            ((Node) node).update();


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

    @Override
    protected LinkBehavior create_link_behavior(Link link) {
        return new LinkBehaviorACTM(link);
    }

    @Override
    protected Node_FlowSolver create_node_flow_solver(Node node) {
        return new Node_FlowSolver_ACTM(node);
    }

}
