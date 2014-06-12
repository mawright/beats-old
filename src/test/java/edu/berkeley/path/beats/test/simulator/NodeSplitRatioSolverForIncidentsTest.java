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

import edu.berkeley.path.beats.jaxb.LinkType;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Node_SplitRatioSolver_ForIncidents;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.BeatsErrorLog.BeatsError;





public class NodeSplitRatioSolverForIncidentsTest {

	// Evaluation fields
	private static ArrayList<BeatsError> log;
	private Method validateCondition;
	private static Field description;
	private static Field errorLog;
	
	// Scenario loading fields
	private Scenario scenario;
	private static String config_folder = "data/config/";
	private String config_file;
	
	// Test environment fields
	Node node = null;
	private Node_SplitRatioSolver_ForIncidents split_ratio_solver;
	
	
	
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
	public void resetTest() throws Exception {
		
		// Reset scenario
		scenario = null;
		config_file = null;
		
		// Clearing Error log
		validateCondition = null;
		BeatsErrorLog.clearErrorMessage();
		
		// Reset environmental fields
		node = null;
		split_ratio_solver = null;	

	}
	
	
	
	/* Test of the validation method */
								 
	// Test: validation number of input links
	@Test
	public void test_inputLinkCondition() throws Exception {
		
		String test_configuration = "Test: validation number of input links.";
		
		// Build test environment
		Node node = buildEnvironment(test_configuration);
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(node);
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue(test_configuration, description.get(log.get(0)).equals("Incorrect number of incomming links at node ID = 0 , total number of incomming links are 2 it must be 1."));

	}
	
	// Test: validation number of output links (low)
	@Test
	public void test_outputLinkConditionLow() throws Exception {
		
		String test_configuration = "Test: validation number of output links (low).";
		
		// Build test environment
		Node node = buildEnvironment(test_configuration);
		
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(node);
		
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue(test_configuration, description.get(log.get(0)).equals("Incorrect number of outgoing links at node ID = 0 , total number of outgoing links are 1 it must be 2."));

	}
	
	// Test: validation number of output links (high)
	@Test
	public void test_outputLinkConditionHigh() throws Exception {
			
		String test_configuration = "Test: validation number of output links (high).";
			
		// Build test environment
		Node node = buildEnvironment(test_configuration);
			
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(node);
			
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue(test_configuration, description.get(log.get(0)).equals("Incorrect number of outgoing links at node ID = 0 , total number of outgoing links are 3 it must be 2."));

	}
	
	// Test: validation of link type on the downstream link
	@Test
	public void test_freewayTypeCondition() throws Exception {
		
		String test_configuration = "Test: validation of link type on the downstream link.";
		
		// Build test environment
		Node node = buildEnvironment(test_configuration);
			
		// Creating Node_SplitRatioSolver 
		split_ratio_solver = new Node_SplitRatioSolver_ForIncidents(node);
			
		// Evaluating test
		validateCondition = split_ratio_solver.getClass().getDeclaredMethod("validate", null);
		validateCondition.setAccessible(true);
		validateCondition.invoke(split_ratio_solver);
		
		assertTrue(test_configuration, description.get(log.get(0)).equals("Missing downstream link of type Freeway at node ID = 0 ,  it must be exactly one link downstream of type Freeway."));
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
        
        // Access fields
        Field input_links = Node.class.getDeclaredField("input_link");
		input_links.setAccessible(true);
		
		Field output_link = Node.class.getDeclaredField("output_link");
		output_link.setAccessible(true);

        // Construct a Node.
        node = constructANode.newInstance(null);
        
        node.setId(0);
        
        // Add links to the Node.
        Link[] input = null;
        Link[] output = null;
        if (configuration.equals("Test: validation number of input links."))
        {
        	// Adding input links
        	input = new Link[2];
        	input[0] = linkBuilder(0,"Freeway");
        	input[1] = linkBuilder(1,"Freeway");
        	
        	// Adding output links
        	output = new Link[2];
        	output[0] = linkBuilder(0,"Freeway");
        	output[1] = linkBuilder(1,"Freeway");
        		
        }
        else if (configuration.equals("Test: validation number of output links (low)."))
        {
        	// Adding input links
        	input = new Link[1];
        	input[0] = linkBuilder(0,"Freeway");
        	
        	// Adding output links
        	output = new Link[1];
        	output[0] = linkBuilder(0,"Freeway");
        		
        }
        else if (configuration.equals("Test: validation number of output links (high)."))
        {
        	// Adding input links
        	input = new Link[1];
        	input[0] = linkBuilder(0,"Freeway");
        	
        	// Adding output links
        	output = new Link[3];
        	output[0] = linkBuilder(0,"Freeway");
        	output[1] = linkBuilder(1,"Freeway");
        	output[2] = linkBuilder(2,"Freeway");
        		
        }
        else if (configuration.equals("Test: validation of link type on the downstream link."))
        {
        	// Adding input links
        	input = new Link[1];
        	input[0] = linkBuilder(0,"Freeway");
        	
        	// Adding output links
        	output = new Link[2];
        	output[0] = linkBuilder(0,"Interconnect");
        	output[1] = linkBuilder(1,"Off-ramp");
        		
        }
        else 
        {
        	throw new Exception("Failed to construct test enviroment.");
        }
        
        input_links.set(node, input);
        output_link.set(node, output);
        
        return node;
	}
	
	// Builds link
	private Link linkBuilder(int link_id, String link_type) throws Exception
	{
		// Access the Link constructor.
		Constructor<Link> constructALink= Link.class.getDeclaredConstructor(null);
        constructALink.setAccessible(true);
        
        // Construct a Link.
        Link link = constructALink.newInstance(null);		
		
        // Set fields.
        link.setId(link_id);
        
        LinkType type = new LinkType();
        type.setName(link_type);
        link.setLinkType(type);
        
		return link;
	}

}
