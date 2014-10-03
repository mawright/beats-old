package edu.berkeley.path.beats.control.rm_interface;

import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.InitialDensitySet;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.SplitRatioSet;

import edu.berkeley.path.beats.jaxb.ActuatorSet;
//import edu.berkeley.path.lprm.lp.RampMeteringSolver;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by gomes on 9/30/2014.
 */
public class RampMeteringPolicyMakerLp implements RampMeteringPolicyMaker {

    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringControlSet control, Double dt, Properties props) {

        double sim_dt_in_seconds = 3d;
        double K_dem_seconds = 9d;
        double K_cool_seconds = 3d;
        double eta = .1d;

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        ActuatorSet actuators = null;

        try {
            edu.berkeley.path.lprm.network.beats.Network lp_net = null;

//            RampMeteringSolver solver = new RampMeteringSolver(lp_net, fd, splitRatios, actuators, K_dem, K_cool, eta, sim_dt_in_seconds);
//        ArrayList<String> errors = policy_maker.getFwy().check_CFL_condition(sim_dt_in_seconds);
//        if (!errors.isEmpty()) {
//            System.err.print(errors);
//            throw new Exception("CFL error");
//        }
//        InitialDensitySet ics = scenario.getInitialDensitySet();
//        DemandSet demands = scenario.getDemandSet();
//        policy_maker.set_data(ics, demands);
//        RampMeteringSolution sol = policy_maker.solve(solver_type);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;

    }


}
