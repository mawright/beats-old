package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by matt on 7/14/15.
 */
public class GeneralNodeModelTest {

	private static Scenario static_scenario;
	private static String config_folder = "data/config/";
	private static String output_folder = "data/test/output/";
	private static String fixture_folder = "data/test/fixture/";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		try {
//			String config_file = "_smalltest.xml";
//			static_scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
//			if(static_scenario==null)
//				fail("scenario did not load");
//
//			// initialize
//			double timestep = Defaults.getTimestepFor(config_file);
//			double starttime = 300d;
//			double endtime = Double.POSITIVE_INFINITY;
//			int numEnsemble = 10;
//			static_scenario.initialize(timestep,starttime,endtime,numEnsemble,"gaussian","general","A");
//			static_scenario.reset();
//
//		} catch (BeatsException e) {
//			fail("initialization failure.");
//		}
	}

	@Test
	public void twoInTwoOutTest() {
		try {
			Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + "_onejunctiontest.xml");
			// initialize
			double timestep = 1;
			double starttime = 0;
			double endtime = 1;
			int numEnsemble = 1;
			scenario.initialize(timestep,starttime,endtime,numEnsemble,"gaussian","general","A");
			scenario.reset();

			double [][] initialDensity = new double[4][1];
			initialDensity[0][0] = 1000;
			initialDensity[1][0] = 1000;
			initialDensity[2][0] = 0;
			initialDensity[3][0] = 0;
			scenario.set.totalDensity(initialDensity);

			scenario.advanceNSeconds(1);
			double [][] X = scenario.get.totalDensity(scenario.getNetworks().get(0).getId());
			double [][] expected = new double[4][1];
			expected[0][0] = 1000 - 600 - 66.67;
			expected[1][0] = 1000 - 533.33;
			expected[2][0] = 600;
			expected[3][0] = 66.67 + 533.33;
			assertEquals(expected[0][0], X[0][0], .01);
			assertEquals(expected[1][0], X[1][0], .01);
			assertEquals(expected[2][0], X[2][0], .01);
			assertEquals(expected[3][0], X[3][0], .01);
		}
		catch (BeatsException e) {
			fail(e.getMessage());
		}

	}
}
