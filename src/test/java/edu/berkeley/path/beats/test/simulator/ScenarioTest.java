package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class ScenarioTest {

	private static Scenario static_scenario;
	private static String config_folder = "data/config/";
//	private static String quarantine_folder = "data/config.quarantine/";
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
			static_scenario.initialize(timestep,starttime,endtime,numEnsemble);
			static_scenario.reset();
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

	@Test
	public void test_initialize_run_advanceNSeconds() {
		try {
			String config_file = "_smalltest.xml";
			Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
			if(scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			scenario.initialize(timestep,starttime,endtime,numEnsemble);
			scenario.reset();

			assertEquals(scenario.get.currentTimeInSeconds(),300d,1e-4);
			assertEquals(scenario.get.numEnsemble(),10,1e-4);
			
			scenario.advanceNSeconds(300d);
			assertEquals(scenario.get.currentTimeInSeconds(),600d,1e-4);
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

//	@Test
//	public void test_saveToXML() {
//		try {
//			String test_file = "test_saveXML.xml";
//			String config_file = "_smalltest_nocontrol.xml";
//			Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
//			if(scenario==null)
//				fail("scenario did not load");
//			
//			scenario.saveToXML(output_folder+test_file);
//			
//			File f1 = new File(output_folder+test_file);
//			File f2 = new File(fixture_folder+test_file);
//			assertTrue("The files differ!", FileUtils.contentEquals(f1, f2));
//			
//		} catch (BeatsException e) {
//			fail("initialization failure.");
//		} catch (IOException e) {
//			fail("IOException.");
//		}
//	}

	@Test
	public void test_time_getters() {
		assertEquals(static_scenario.get.currentTimeInSeconds(),300d,1e-4);
		assertEquals(static_scenario.get.timeElapsedInSeconds(),0d,1e-4);
//		assertEquals(static_scenario.getCurrentTimeStep(),0,1e-4);
//		assertEquals(static_scenario.getTotalTimeStepsToSimulate(),-1,1e-4);
	}
	
//	@Test
//	public void test_set_timestep() {
//		try {
//			String config_file = "_smalltest_withdemandprofile.xml";
//			Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
//			if(scenario==null)
//				fail("scenario did not load");
//
//			// initialize
//			double timestep = 5;
//			double starttime = 0d;
//			double endtime = Double.POSITIVE_INFINITY;
//			int numEnsemble = 1;
//			scenario.initialize(timestep,starttime,endtime,numEnsemble);
//
//			assertEquals(scenario.get.currentTimeInSeconds(),0d,1e-4);
//			scenario.advanceNSeconds(200d);
//
//			double demand1 = scenario.get.linkWithId(-6).getDemandProfile().getCurrentValue(0)[0];
//
//			double[][] densityAt200 = scenario.get.totalDensity(-1);
//
//			scenario.advanceNSeconds(300d);
//			double demand2 = scenario.get.linkWithId(-6).getDemandProfile().getCurrentValue(0)[0];
//			double[][] densityAt500Round1 = scenario.get.totalDensity(-1);
//
//			assertFalse(scenario.get.linkWithId(-6).getDemandProfile().getCurrentValue(0)[0]==demand1);
//
//			scenario.set.timeInSeconds(200);
//			scenario.set.totalDensity(densityAt200);
//			assertEquals(scenario.get.linkWithId(-6).getDemandProfile().getCurrentValue(0)[0],demand1,1e-4);
//
//			scenario.advanceNSeconds(300d);
//			assertEquals(scenario.get.linkWithId(-6).getDemandProfile().getCurrentValue(0)[0],demand2,1e-4);
//
//			double[][] densityAt500Round2 = scenario.get.totalDensity(-1);
//
//			for (int i=0;i<densityAt500Round1.length;i++)
//				assertEquals(densityAt500Round1[i][0], densityAt500Round2[i][0],1e-4);
//
//		} catch (BeatsException e) {
//			fail("initialization failure.");
//		}
//	}

	@Test
	public void test_get_numVehicleTypes() {
		assertEquals(static_scenario.get.numVehicleTypes(),1,1e-4);
	}

	@Test
	public void test_get_numEnsemble() {
		assertEquals(static_scenario.get.numEnsemble(),10,1e-4);
	}

	@Test
	public void test_getVehicleTypeIndex() {
		assertEquals(static_scenario.get.vehicleTypeIndexForName("car"),0);
		assertEquals(static_scenario.get.vehicleTypeIndexForName("xxx"),-1);
		
		// edge case
		assertEquals(static_scenario.get.vehicleTypeIndexForName(null),-1);
	}
	
	@Test
	public void test_get_simdtinseconds() {
		assertEquals(static_scenario.get.simdtinseconds(),5,1e-4);
	}

	@Test
	public void test_get_timeStart() {
		assertEquals(static_scenario.get.timeStart(),300d,1e-4);
	}

	@Test
	public void test_get_timeEnd() {
		assertTrue(Double.isInfinite(static_scenario.get.timeEnd()));
	}

	@Test
	public void test_get_configFilename() {
		assertEquals(static_scenario.get.configFilename(),config_folder+"_smalltest.xml");
	}

	@Test
	public void test_get_vehicleTypeNames() {
		String [] names = static_scenario.get.vehicleTypeNames();
		assertEquals(names[0],"car");
	}

	@Test
	public void test_get_densityForNetwork() {
		double x = static_scenario.get.densityForNetwork(-1,0)[0][0];
		double exp =0.4445728212287675;
		assertEquals(x,exp,1e-4);

		//x = static_scenario.get.densityForNetwork(null,0)[0][0];	// null works for single networks
		//assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.get.densityForNetwork(-100000,0));
		assertNull(static_scenario.get.densityForNetwork(-1,-1));
		assertNull(static_scenario.get.densityForNetwork(-1,100));
	}

	@Test
	public void test_get_linkWithId() {
		Link link = static_scenario.get.linkWithId(-1);
		double x = link.getLengthInMeters();
		double exp = 429.2823615191171;
		assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.get.linkWithId(-100000));
	}
	
	@Test
	public void test_getNodeWithId() {
		Node node =  static_scenario.get.nodeWithId(-2);
		double x = node.getPosition().getPoint().get(0).getLat();
		double exp  =37.8437831193107;
		assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.get.nodeWithId(-100000));
	}

	@Test
	public void test_get_Controller_Event_Sensor_WithId() {
		try {
			String config_file = "complete.xml";
			Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
			if(scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			scenario.initialize(timestep,starttime,endtime,numEnsemble);
			scenario.reset();
			
			assertNotNull(scenario.get.controllerWithId(1));
			assertNotNull(scenario.get.eventWithId(1));
			assertNotNull(scenario.get.sensorWithId(1));

			assertNull(scenario.get.controllerWithId(-100000));
			assertNull(scenario.get.eventWithId(-100000));
			assertNull(scenario.get.sensorWithId(-100000L));
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

	@Test
	public void test_addController() {
		
	}

	@Test
	public void test_addEvent() {
	}

	@Test
	public void test_addDemandProfile() {
	}

	@Test
	public void test_loadSensorData() {
	}

	@Test
	public void test_calibrate_fundamental_diagrams() {
	}

    @Test
    public void test_getSensorWithVDS() {
        int vds = 100;
        Sensor sensor = static_scenario.get.sensorWithVDS(vds);
        long exp = -2;
        assertEquals(sensor.getId(),exp,1e-4);

        // edge cases
        assertNull(static_scenario.get.sensorWithVDS(0));

    }

    @Test
    public void test_getSensors() {
        List<Sensor> sensorlist = static_scenario.get.sensors();
        System.out.println(sensorlist);
    }
	
}
