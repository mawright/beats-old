package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/28/14.
 */
public class NodeBehavior {

    protected Node_SplitRatioSolver sr_solver;
    protected Node_FlowSolver flow_solver;
    protected Node_SupplyPartitioner supply_partitioner;
    public NodeBehavior(Node_SplitRatioSolver sr_solver,Node_FlowSolver flow_solver,Node_SupplyPartitioner supply_partitioner){
        this.supply_partitioner = supply_partitioner;
        this.sr_solver = sr_solver;
        this.flow_solver = flow_solver;
    }

}
