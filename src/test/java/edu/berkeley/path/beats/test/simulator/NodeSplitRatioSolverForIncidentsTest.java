package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;



public class NodeSplitRatioSolverForIncidentsTest {

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {


	}
	
	/* Test of the validation method */
	@Test
	public void test_inputLinkCondition() {
		// TODO: Implement code
		// This test will check the error made by violating myNode.input_link.length != 1
	}
	
	@Test
	public void test_outputLinkCondition() {
		// TODO: Implement code
		// This test will check the error made by violating myNode.output_link.length != 2
	}
	
	@Test
	public void test_freewayTypeCondition() {
		// TODO: Implement code
		// This test will check the error made by violating fwy_id == -1
	}
	
	@Test
	public void test_divergingTypeCondition() {
		// TODO: Implement code
		// This test will check the error made by violating off_ramp_id == -1
	}
	
	
	
	/* Test of calculation method */
	
	@Test
	public void test_negativeSplitRatioWarning() {
		// TODO: Implement code
		// This test will check the warning raised by an adjustment
		// that leads to negative split ratio.
		// Check both warning and that the output is sr_new = 0.
	}
	
	@Test
	public void test_overBoundedSplitRatioWarning() {
		// TODO: Implement code
		// This test will check the warning raised by an adjustment
		// that leads to a split ratio above 1.
		// Check both warning and that the output is sr_new = 1.
	}
	
	@Test
	public void test_caluclationOneVehOneEnsemble() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}
	
	@Test
	public void test_caluclationTwoVehOneEnsemble() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}
	
	@Test
	public void test_caluclationTwoVehTwoEnsembles() {
		// TODO: Implement code
		// This test will check that the right output are calculated.
		// Check both under and over threshold.
	}

}
