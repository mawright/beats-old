package edu.berkeley.path.beats.simulator;

import java.util.List;

/**
 * Created by gomes on 10/26/14.
 */
public class ScenarioUpdaterACTM extends ScenarioUpdaterAbstract {

    private List<FwyNode> fwy_nodes;

    public ScenarioUpdaterACTM(Scenario scenario){
        super(scenario,null,"A");
    }

    @Override
    public void populate() {
        super.populate();

        // find first node
        List<Node> first_fwy_nodes = ((Network)scenario.getNetworkSet().getNetwork().get(0)).get_terminal_freeway_nodes();



    }

    @Override
    public void update() throws BeatsException {

        // CHECK NUMENSEMBLE == 1
        // CHECK NUMVEHICLETYPES = 1
        int e=0;
        int vt = 0;
        double xi = 0.1;
        double gamma = 0.1;

        update_profiles();

        update_sensors_control_events();

        // compute demand and total supply for all links
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork())
            for(edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()) {
                Link bLink = (Link)link;
                bLink.updateOutflowDemand();
                bLink.link_behavior.update_total_space_supply();
            }

        // mainline allocation for onramps only: xi*(njam-n)
        for(FwyNode fwy_node : fwy_nodes )
            if(fwy_node.onramp!=null && fwy_node.dn_ml!=null) {
                fwy_node.supply_for_onramp = xi * fwy_node.dn_ml.link_behavior.total_space_supply[e];
            }

        // onramp flow = min(onramp demand,mainline supply for onramp)
        for (FwyNode fwy_node : fwy_nodes){
            if(fwy_node.onramp!=null){
                double[] demand = fwy_node.onramp.get_out_demand_in_veh(e);
                double total_demand = BeatsMath.sum(demand);
                double ratio = Math.min(1d,fwy_node.supply_for_onramp/total_demand);
                fwy_node.r = Math.min(total_demand,fwy_node.supply_for_onramp);
                fwy_node.onramp.setOutflow(e, BeatsMath.times(demand,ratio) );
            }
        }

        // available supply for mainline = min( w*(totalsupply - gamma*r ) , F )
        for(FwyNode fwy_node : fwy_nodes){
            Link link = fwy_node.dn_ml;
            if(link==null)
                continue;
            FundamentalDiagram FD = link.currentFD(e);
            double r = fwy_node.onramp==null ? 0d : fwy_node.r;
            double w = FD.getWNormalized();
            double F = FD._getCapacityInVeh();
            link.link_behavior.available_space_supply[e] = Math.min( w*(link.get_total_space_supply_in_veh(e) - gamma*r) , F );
        }

        // mainline flow
        for(FwyNode fwy_node : fwy_nodes){

            Link link = fwy_node.dn_ml;
            if(link==null)
                continue;

            Node node = fwy_node.node;

            // update split ratio matrix
            Double3DMatrix[] splitratio_selected = node.select_and_perturb_split_ratio();

            double fr_split = splitratio_selected[e].get(fwy_node.up_ml_index,fwy_node.fr_index,vt);




            Node_FlowSolver.IOFlow IOflow = node.node_behavior.flow_solver.computeLinkFlows(splitratio_selected[e],e);

            if(IOflow==null)
                return;

            // assign flow to input links ..................................
            for(int i=0;i< node.nIn;i++)
                node.input_link[i].setOutflow(e,IOflow.getIn(i));

            // assign flow to output links .................................
            for (int j=0;j< node.nOut;j++)
                node.output_link[j].setInflow(e,IOflow.getOut(j));
        }

        // update density
        for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork())
            update_density((Network) network);

        update_cumalitives_clock();
    }

    @Override
    protected LinkBehavior create_link_behavior(Link link) {
        return null;
    }

    @Override
    protected Node_FlowSolver create_node_flow_solver(Node node) {
        return null; //new Node_FlowSolver_ACTM(node);
    }


    public class FwyNode {
        Link dn_ml;
        Node node;
        Link onramp;
        double r;
        double supply_for_onramp;
        int up_ml_index;
        int fr_index;
    }

}
