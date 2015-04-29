package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;


import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.*;

import edu.berkeley.path.beats.simulator.utils.BeatsException;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeStochasticTest {

	private static Node node;
	private static String config_folder = "data/config/";
	private static String config_file = "_smalltest_stochasticsplits.xml";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Scenario scenario = Jaxb.create_scenario_from_xml(config_folder + config_file);
		if(scenario==null)
			fail("scenario did not load");
		
		double timestep = 5;
		double starttime = 0;
		double endtime = 300;
		int numEnsemble = 2;
		scenario.initialize(timestep,starttime,endtime,numEnsemble);
		node = scenario.get.nodeWithId(-4);
	}

	@Test
	public void test_sampledSplitsFromConcentrationParams() {
		Link[] outlinks = node.getOutput_link();
		
		Scenario scenario = node.getMyNetwork().getMyScenario();
		try {
			scenario.advanceNSeconds(300d);
		} catch (BeatsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(outlinks[0].getTotalDensityInVeh(0)!=outlinks[0].getTotalDensityInVeh(1));
		assertTrue(outlinks[1].getTotalDensityInVeh(0)!=outlinks[1].getTotalDensityInVeh(1));
	
	}
	
}
