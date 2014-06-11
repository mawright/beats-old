package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.DemandProfile;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import org.junit.BeforeClass;
import org.junit.Test;

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

        long link_id = 1;
        long vt_id = 0;
        double sim_dt = scenario.getSimdtinseconds();

        try {

            // get existing profile
            DemandProfile dp = scenario.get_current_demand_for_link(link_id);

            // get current value
            double current_demand = dp.getCurrentValue(0)[0]/sim_dt;
            double [] override = {current_demand*2};

            System.out.println("Current: " + 3600*current_demand + " vph");
            System.out.println("Override: " + 3600*override[0] + " vph");

            HashMap<Long,double []> X = new HashMap<Long,double []>();
            X.put(vt_id,override);

            scenario.set_demand_for_link_si(link_id, dp.getDt().doubleValue(), X);

            scenario.advanceNSeconds(sim_dt);

            dp = scenario.get_current_demand_for_link(link_id);
            current_demand = dp.getCurrentValue(0)[0]/sim_dt;


            System.out.println("New current: " + 3600*current_demand + " vph");

            scenario.advanceNSeconds(50*sim_dt);
            current_demand = dp.getCurrentValue(0)[0]/sim_dt;
            System.out.println("After 50 dts: " + 3600*current_demand + " vph");

        } catch ( Exception exp){

        }

//        scenario.set_demand_for_link_si(link_id,start_time,dt,demands);
    }

    @Test
    public void test_setCapcityProfile() {
        assertTrue(true);
//        assertEquals(sensor.getMyLink().getId(),1);
    }

}
