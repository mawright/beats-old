package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.jaxb.Demand;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.JaxbObjectFactory;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by gomes on 6/9/2014.
 */
public class SetProfileTest {

    private static Scenario scenario;
    private static String config_folder = "data/config/";
    private static String config_file = "complete.xml";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        scenario = ObjectFactory.createAndLoadScenario(config_folder + config_file);
        if(scenario==null)
            fail("scenario did not load");

        double timestep = Defaults.getTimestepFor(config_file);
        double starttime = 0;
        double endtime = 300;
        int numEnsemble = 1;
        scenario.initialize(timestep,starttime,endtime,numEnsemble);

        scenario.advanceNSeconds(100);
    }

    @Test
    public void test_setDemandProfile() {
        long link_id = 0;
        double start_time = 100;
        double dt = 300;
        HashMap<Long,Double []> demands = new HashMap<Long,Double []>();

        Double [] d = new Double[4];
        d[0] = 100d;
        d[1] = 200d;
        d[2] = 300d;
        d[3] = 400d;
        demands.put(0L,d);

        scenario.set_demand_profile(link_id,start_time,dt,demands);
    }

    @Test
    public void test_setCapcityProfile() {
        assertTrue(true);
//        assertEquals(sensor.getMyLink().getId(),1);
    }

}
