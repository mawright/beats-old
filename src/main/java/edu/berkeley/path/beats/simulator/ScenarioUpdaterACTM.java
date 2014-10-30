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

//        update_profiles();
//
//        update_sensors_control_events();
//
//        // compute demand and total supply for all links
//        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork())
//            for(edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()) {
//                Link bLink = (Link)link;
//                bLink.updateOutflowDemand();
//                bLink.link_behavior.update_total_space_supply();
//            }
//
//        // mainline allocation for onramps only: xi*(njam-n)
//        for(Link link : mainline_links_with_onramp )
//            ((LinkBehaviorACTM) link.link_behavior).update_available_space_supply_for_onramp();
//
//        // onramp flow
//        for (Link link : onramp_links ){
//            Node node = link.getEnd_node();
//            Node_FlowSolver_ACTM flow_solver = (Node_FlowSolver_ACTM) node.node_behavior.flow_solver;
//            double onramp_flow = flow_solver.compute_onramp_flow(splitratio_applied, e);
//            link.setOutflow(e,onramp_flow);
//        }
//
//        // rest supply
//        for (Link link : not_onramp_links){
//            link.updateOutflowDemand();
//            ((LinkBehaviorACTM) link.link_behavior).update_available_space_supply_for_mainline();
//        }
//
//        // mainline flow
//        for (Node node : mainline_nodes){
//            ???
//        }
//
//        // update density
//        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork())
//            update_density(network);
//
//        update_cumalitives_clock();
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
