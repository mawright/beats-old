package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/26/14.
 */
public abstract class ScenarioUpdaterAbstract implements ScenarioUpdaterInterface {

    protected Scenario scenario;

    protected TypeNodeFlowSolver nodeflowsolver;
    protected TypeNodeSplitSolver nodesrsolver;

    public ScenarioUpdaterAbstract(Scenario scenario,String nodeflowsolver_name,String nodesrsolver_name){
        this.scenario = scenario;
        if(nodeflowsolver_name!=null)
            this.nodeflowsolver = TypeNodeFlowSolver.valueOf(nodeflowsolver_name);
        if(nodesrsolver_name!=null)
            this.nodesrsolver = TypeNodeSplitSolver.valueOf(nodesrsolver_name);
    }

    @Override
    public void populate() {

        for( edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork() ){

            // set link behaviors
            for(edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()){
                Link blink = (Link)link;
                blink.link_behavior =  create_link_behavior(blink);
            }

            // set node behaviors
            for(edu.berkeley.path.beats.jaxb.Node node : network.getNodeList().getNode()){
                Node bNode = (Node)node;
                bNode.set_node_split_solver(create_node_sr_solver(bNode));
                bNode.set_node_flow_solver(create_node_flow_solver(bNode));
            }
        }
    }

    protected LinkBehavior create_link_behavior(Link link){
        LinkBehavior link_behavior;
        switch(link.link_type){
            case freeway:
                link_behavior = new LinkBehaviorCTM(link);
                break;
            case intersection_approach:
                link_behavior = new LinkBehaviorQueue(link);
                break;
            case street:
                link_behavior = new LinkBehaviorQueueAndTravelTime(link);
                break;
            default:
                link_behavior = new LinkBehaviorCTM(link);
                break;
        }
        return link_behavior;
    }

    protected Node_SplitRatioSolver create_node_sr_solver(Node node){

        // create node split ratio solver
        Node_SplitRatioSolver node_sr_solver;
        switch(nodesrsolver){
            case A:
                node_sr_solver = new Node_SplitRatioSolver_A(node);
                break;
            case B:
                node_sr_solver = new Node_SplitRatioSolver_B(node);
                break;
            case C:
                node_sr_solver = new Node_SplitRatioSolver_C(node);
                break;
            case HAMBURGER:
                Parameters param = (Parameters) node.getNodeType().getParameters();
                if(param!=null && param.has("threshold") && param.has("scaling_factor"))
                    node_sr_solver = new Node_SplitRatioSolver_HAMBURGER(node);
                else
                    node_sr_solver = new Node_SplitRatioSolver_A(node);
                break;
            default:
                node_sr_solver = new Node_SplitRatioSolver_A(node);
        }
        return node_sr_solver;
    }

    protected Node_FlowSolver create_node_flow_solver(Node node){
        Node_FlowSolver node_flow_solver;
        switch(nodeflowsolver) {
            case proportional:
                node_flow_solver = new Node_FlowSolver_LNCTM(node);
                break;
            case symmetric:
                node_flow_solver = new Node_FlowSolver_Symmetric(node);
                break;
            case actm:
                node_flow_solver = new Node_FlowSolver_ACTM(node);
                break;
            default:
                node_flow_solver = new Node_FlowSolver_LNCTM(node);
        }
        return node_flow_solver;
    }

}
