package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import org.junit.BeforeClass;
import org.junit.Test;

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
		try {
			String config_file = "_smalltest.xml";
			static_scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
			if(static_scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			static_scenario.initialize(timestep,starttime,endtime,numEnsemble,"gaussian","general","A");
			static_scenario.reset();

		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

	@Test
	public void doesItRunTest() {
		try {
			Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + "_smalltest.xml");
			// initialize
			double timestep = Defaults.getTimestepFor("_smalltest.xml");
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 1;
			scenario.initialize(timestep,starttime,endtime,numEnsemble,"gaussian","general","A");
			scenario.reset();

			scenario.advanceNSeconds(300);
		}
		catch (BeatsException e) {
			fail(e.getMessage());
		}
	}
}
