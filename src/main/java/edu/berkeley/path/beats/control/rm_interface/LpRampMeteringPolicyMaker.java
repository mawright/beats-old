package edu.berkeley.path.beats.control.rm_interface;

import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.InitialDensitySet;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.SplitRatioSet;

import java.util.Properties;

/**
 * Created by gomes on 9/30/2014.
 */
public class LpRampMeteringPolicyMaker implements RampMeteringPolicyMaker {


    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringControlSet control, Double dt, Properties props) {
        return null;
    }


}
