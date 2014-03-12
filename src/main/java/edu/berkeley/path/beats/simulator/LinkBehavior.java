package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 3/12/14.
 */
public abstract class LinkBehavior implements LinkBehaviorInterface {

    protected Link myLink;
    protected Scenario myScenario;
    protected double [] spaceSupply;        // [veh]	numEnsemble
    protected double [][] outflowDemand;    // [veh] 	numEnsemble x numVehTypes

    public LinkBehavior(Link link){
        this.myLink = link;
        this.myScenario = myLink.myScenario;
    }

    /////////////////////////////////////////////////////////////////////
    // protected API
    /////////////////////////////////////////////////////////////////////

    protected void reset(double [] initial_density) {
        int n1 = myScenario.getNumEnsemble();
        int n2 = myScenario.getNumVehicleTypes();
        outflowDemand 	= BeatsMath.zeros(n1,n2);
        spaceSupply 	= BeatsMath.zeros(n1);
        initialize_density(initial_density);
    }

    protected double getTotalDensityInVPMeter(int ensemble) {
        return getTotalDensityInVeh(ensemble)/myLink._length;
    }

    protected double[] get_out_demand_in_veh(int ensemble) {
        return outflowDemand[ensemble];
    }

    protected double get_space_supply_in_veh(int ensemble) {
        return spaceSupply[ensemble];
    }


}
