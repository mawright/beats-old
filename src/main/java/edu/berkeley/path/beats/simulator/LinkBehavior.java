package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public abstract class LinkBehavior implements LinkBehaviorInterface {

    protected Link myLink;
    protected Scenario myScenario;
    protected double [] space_supply;     // [veh]	numEnsemble
    protected double [][] flow_demand;    // [veh] 	numEnsemble x numVehTypes

    public LinkBehavior(Link link){
        this.myLink = link;
        this.myScenario = myLink.myScenario;
    }

    protected void reset(double [] initial_density) {
        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();
        flow_demand = BeatsMath.zeros(n1,n2);
        space_supply = BeatsMath.zeros(n1);
        reset_density();
        for(int e=0;e<n1;e++)
            set_density_in_veh(e,initial_density);
    }

}
