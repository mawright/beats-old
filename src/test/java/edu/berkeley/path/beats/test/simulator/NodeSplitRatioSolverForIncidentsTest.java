package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;






import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Node_SplitRatioSolver_ForIncidents;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.BeatsErrorLog.BeatsError;





public class NodeSplitRatioSolverForIncidentsTest {

	private Node node;
	private Node_SplitRatioSolver_ForIncidents split_ratio_solver;
	private static ArrayList<BeatsError> log;
	private Method validateCondition;
	private static Field description;
	private static Field errorLog;
	private Scenario scenario;
	private static String config_folder = "data/config/";
	private String config_file;
	
	/* Initiation */
	@BeforeClass 
	public static void reflectErrorLog() throws Exception {
		
		// Gives access to the error log.
		errorLog = BeatsErrorLog.class.getDeclaredField("error");
		errorLog.setAccessible(true);
		
		// Storing a reference to the error log
		log = (ArrayList<BeatsError>) errorLog.get(BeatsErrorLog.class);
		
		// Gives access to the error message
		description = BeatsError.class.getDeclaredField("description");
		description.setAccessible(true);

	}
	
	/* Preparing for test */
	@Before
	public void setUpBeforeClass() throws Exception {
		
		// Reset scenario
		scenario = null;
		config_file = null;
		
		// Clearing Error log
		BeatsErrorLog.clearErrorMessage();
		
		// Reset Node_SplitRatioSolver
		validateCondition = null;
		
		
		

	}
	
	
	
	/* Test of the validation method */
								 
	// Test: validation number of input links
	@Ignore
	public void test_inputLinkCondition() throws Exception {
		
		String test_configuration = "Test: validation number of input links.";
		// Build test environment
		
		node = buildEnvironment(test_configuration);
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(scenario.getNodeWithId(0));
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue(test_configuration, description.get(log.get(0)).equals("Incorrect number of incomming links at node ID = 0 , total number of incomming links are 2 it must be 1."));

	}
	
	// Test: validation number of output links
	@Ignore
	public void test_outputLinkCondition() throws Exception {
		
		
		// Loading scenario
		
		config_file = "incident_split_ratio_test_two_to_two.xml";
		scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");	

		double timestep = Defaults.getTimestepFor(config_file);
		double starttime = 300d;
		double endtime = Double.POSITIVE_INFINITY;
		int numEnsemble = 1;
		scenario.initialize(timestep,starttime,endtime,numEnsemble);
		scenario.reset();
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(scenario.getNodeWithId(0));
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		//assertTrue("Test: validation number of output links.", description.get(log.get(0)).equals("Incorrect number of outgoing links at node ID = 0 , total number of outgoing links are 1 it must be 2."));
		
	}
	
	// Validation of link type on the downstream link
	@Ignore
	public void test_freewayTypeCondition() throws Exception {
		
		// Loading scenario
		config_file = "incident_split_ratio_test_two_to_two.xml";
		scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");	

		double timestep = Defaults.getTimestepFor(config_file);
		double starttime = 300d;
		double endtime = Double.POSITIVE_INFINITY;
		int numEnsemble = 1;
		scenario.initialize(timestep,starttime,endtime,numEnsemble);
		scenario.reset();
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(scenario.getNodeWithId(0));
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue("validation of link type on the downstream link.", description.get(log.get(0)).equals("Missing downstream link of type Freeway at node ID = 0 ,  it must be exactly one link downstream of type Freeway."));
	}
	
	// Validation of link type on the diverging link
	@Ignore
	public void test_divergingTypeCondition() throws Exception {
		
		// Loading scenario
		config_file = "incident_split_ratio_test_two_to_two.xml";
		scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");	

		double timestep = Defaults.getTimestepFor(config_file);
		double starttime = 300d;
		double endtime = Double.POSITIVE_INFINITY;
		int numEnsemble = 1;
		scenario.initialize(timestep,starttime,endtime,numEnsemble);
		scenario.reset();
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(scenario.getNodeWithId(0));
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue("Test: validation of link type on the diverging link.", description.get(log.get(0)).equals("Missing diverging link of type Off-ramp/Interconnect at node ID = 0 ,  it must be exactly one diverging link of type Off-ramp or Interconnect."));
	}
	
	
	
	/* Test of calculation */
	
	
	@Ignore
	public void test_negativeSplitRatioWarning() {
		// TODO: Implement code
		// This test will check the warning raised by an adjustment
		// that leads to negative split ratio.
		// Check both warning and that the output is sr_new = 0.
	}
	
	@Ignore
	public void test_overBoundedSplitRatioWarning() {
		// TODO: Implement code
		// This test will check the warning raised by an adjustment
		// that leads to a split ratio above 1.
		// Check both warning and that the output is sr_new = 1.
	}
	
	@Ignore
	public void test_caluclationOneVehOneEnsemble() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}
	
	@Ignore
	public void test_caluclationTwoVehOneEnsemble() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}
	
	@Ignore
	public void test_caluclationTwoVehTwoEnsembles() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}


	/* Utility methods */
	
	// Builds a network based on a configuration
	private Node buildEnvironment(String configuration) throws Exception 
	{
		// Access the Node constructor.
		Constructor<Node> constructANode= Node.class.getDeclaredConstructor(null);
        constructANode.setAccessible(true);
        
        // Access Node fields.
        
        // Construct a Node.
        Node node = constructANode.newInstance(null);
        
        // Add links to the Node.
        
        
        return node;
	}
	
	// Builds link
	private Link linkBuilder(int link_id, String link_type) throws Exception
	{
		// Access the Link constructor.
		Constructor<Link> constructALink= Link.class.getDeclaredConstructor(null);
        constructALink.setAccessible(true);
        
        // Access the Link fields.
        
        // Construct a Link.
        Link link = constructALink.newInstance(null);
		
        // Set fields.
        
		return link;
	}

}
