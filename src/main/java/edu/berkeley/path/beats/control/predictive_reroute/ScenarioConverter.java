package edu.berkeley.path.beats.control.predictive_reroute;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import edu.berkeley.path.beats.jaxb.DemandSet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.jaxb.InitialDensitySet;
import edu.berkeley.path.beats.jaxb.Network;
import edu.berkeley.path.beats.jaxb.RouteSet;
import edu.berkeley.path.beats.jaxb.SplitRatioSet;
import dtapc.BeATS_interface;

public class ScenarioConverter implements ReroutePolicyMaker {
	
	public ReroutePolicySet givePolicy(Network net,
            FundamentalDiagramSet fd,
            DemandSet demand,
            SplitRatioSet splitRatios,
            InitialDensitySet ics,
            RouteSet routes,
            Double dt) {
	
	double[] policy = BeATS_interface.computePolicy(net,
            fd,
            demand,
            splitRatios,
            ics,
            routes,
            dt);
	
	ReroutePolicySet reroutePolicySet = new ReroutePolicySet();
	Double[] policyDouble = ArrayUtils.toObject(policy);
	ReroutePolicyProfile reroutePolicyProfile = new ReroutePolicyProfile();
	reroutePolicyProfile.reroutePolicy = Arrays.asList(policyDouble);
	reroutePolicySet.profiles.add(reroutePolicyProfile);
	
	return reroutePolicySet;
	}
}
