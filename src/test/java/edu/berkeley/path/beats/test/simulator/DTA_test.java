package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.control.adjoint_glue.AdjointReroutesPolicyMaker;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DTA_test {

    private static String configfilename = "data/config/Rerouting_sent_newxsd_v3.xml";
    private static Scenario scenario;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            scenario = ObjectFactory.createAndLoadScenario(configfilename);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void test_getDensityForLinkIdInVeh() {

//        double[] result = AdjointReroutesPolicyMaker.computePolicy(
//                scenario.getNetworkSet().getNetwork().get(0),
//                scenario.getFundamentalDiagramSet(),
//                scenario.getDemandSet(),
//                scenario.getSplitRatioSet(),
//                scenario.getInitialDensitySet(),
//                scenario.getRouteSet(),
//                60d);
//
//        org.junit.Assert.assertNotNull(result);
    }


}
