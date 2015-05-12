package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.Runner;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by gomes on 5/11/2015.
 */
public class FrModeTest {





    @Test
    public void testWithProperties() throws Exception {
        try {
            //String config_file = "C:\\Users\\gomes\\Dropbox\\_work_other\\polytechnique\\calibration\\config\\210E_joined_frmode.xml";

            String propsfile = "C:\\Users\\gomes\\Dropbox\\_work_other\\polytechnique\\calibration\\config\\frmode.properties";

            Scenario scenario = Runner.load_scenario_from_properties(propsfile);



            scenario.set.demand_knob_for_link_id(128794214, 40);


            // run the scenario
            scenario.run();

        } catch (BeatsException e) {
            fail("initialization failure.");
        }
    }
}
