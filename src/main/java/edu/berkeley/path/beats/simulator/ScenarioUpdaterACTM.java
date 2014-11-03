package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;
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

        if(first_fwy_nodes.size()!=1)
            return;

        Node current_node = first_fwy_nodes.get(0);
        int c = 0;
        fwy_nodes = new ArrayList<FwyNode>();
        while(true){

            FwyNode fwynode = new FwyNode();
            fwy_nodes.add(fwynode);

            fwynode.node = current_node;

            for(int i=0;i<current_node.output_link.length;i++){
                Link link = current_node.output_link[i];
                if(link.link_type==Link.Type.freeway)
                    fwynode.dn_ml = link;
                if(link.link_type==Link.Type.offramp)
                    fwynode.fr_index = i;
            }

            for(int i=0;i<current_node.input_link.length;i++) {
                Link link = current_node.input_link[i];
                if (link.link_type == Link.Type.onramp)
                    fwynode.onramp = link;
                if (link.link_type == Link.Type.freeway)
                    fwynode.up_ml_index = i;
            }

            if(fwynode.dn_ml==null){
                System.out.println("Reached end of freeway at node " + current_node.getId());
                break;
            }

            if(c++ > 1000){
                System.out.println("Exceeded maximum number of freeway segments. Possibly a loop exists.");
                break;
            }

            current_node = fwynode.dn_ml.getEnd_node();
        }

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

        // available supply for mainline = w*(totalsupply - gamma*r )
        for(FwyNode fwy_node : fwy_nodes){
            Link link = fwy_node.dn_ml;
            if(link==null)
                continue;
            FundamentalDiagram FD = link.currentFD(e);
            double r = fwy_node.onramp==null ? 0d : fwy_node.r;
            double w = FD.getWNormalized();
            link.link_behavior.available_space_supply[e] = w*(link.get_total_space_supply_in_veh(e) - gamma*r);
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

            fr_split = Double.isNaN(fr_split) ? 0d : fr_split;




            Node_FlowSolver.IOFlow IOflow = node.node_behavior.flow_solver.computeLinkFlows(splitratio_selected[e],e);

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
    protected LinkBehaviorCTM create_link_behavior(Link link) {
        return new LinkBehaviorACTM(link);
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

        @Override
        public String toString() {
            return dn_ml==null ? "-" : String.format("%d",dn_ml.getId());
        }
    }

}
