package edu.berkeley.path.beats.control.rm_interface;

import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.*;

import edu.berkeley.path.beats.jaxb.ActuatorSet;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
//import edu.berkeley.path.lprm.lp.RampMeteringSolver;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by gomes on 9/30/2014.
 */
public class RampMeteringPolicyMakerLp implements RampMeteringPolicyMaker {

    private RampMeteringSolver solver;
    private double sim_dt_in_seconds;

    public RampMeteringPolicyMakerLp(Scenario myScenario,double K_dem_seconds,double K_cool_seconds,double eta){
        sim_dt_in_seconds = myScenario.getSimdtinseconds();
        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);
        try {
            solver = new RampMeteringSolver(myScenario, K_dem, K_cool, eta, sim_dt_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringControlSet control, Double dt, Properties props) {
        try {
            solver.set_data(ics,demand);
            RampMeteringSolution sol = solver.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;

    }


}
