package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by gomes on 5/11/2015.
 */
public class FrModeTest {

    @Test
    public void testFrMode() {
        try {
            Scenario scenario = Jaxb.create_scenario_from_xml("data" + File.separator + "config" + File.separator + "_smalltest_SRcontrol2.xml");
            if (scenario == null)
                fail("scenario did not load");
            scenario.initialize(5d, 0d, 3600d, 5d, "", "", 1, 1, null, null, null, null, "fw_fr_split_output", null, null, null);
            scenario.run();
            Link link = scenario.get.linkWithId(-7);
            Double d = BeatsMath.sum(link.getOutflowInVeh(0)) * 3600d / 5d;
            assertTrue(BeatsMath.equals(d, 150d));
        } catch (BeatsException e) {
            e.printStackTrace();
        }
    }
}
