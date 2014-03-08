package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;

import edu.berkeley.path.beats.actuator.ActuatorSignal;
import edu.berkeley.path.beats.actuator.NEMA;
import edu.berkeley.path.beats.control.SignalCommand;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.actuator.ActuatorSignal.SignalPhase;

@Ignore("redo signals")
public class SignalTest {

	private static ActuatorSignal signal;
	private static String config_folder = "data/config/";
	private static String config_file = "Albany-and-Berkeley.xml";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
//		if(scenario==null)
//			fail("scenario did not load");
//
//		// initialize
//		double timestep = Defaults.getTimestepFor(config_file);
//		double starttime = 0;
//		double endtime = 300;
//		int numEnsemble = 1;
//		scenario.initialize(timestep,starttime,endtime,numEnsemble);
//
//		signal = (ActuatorSignal) scenario.getSignalWithId(-12);
	}

	@Test
	public void test_getPhaseByNEMA() {
		
		assertNotNull(signal.get_phase_with_nema(NEMA.ID._2));
		assertNull(signal.get_phase_with_nema(NEMA.ID.NULL));
		
		// edge case
		assertNull(signal.get_phase_with_nema(null));
	}

	@Test
	public void test_requestCommand() {
		ArrayList<SignalCommand> command = new ArrayList<SignalCommand>();
		NEMA.ID nema = NEMA.ID._2;
		SignalPhase phase = signal.get_phase_with_nema(nema);
		command.add( new SignalCommand(SignalCommand.Type.forceoff,nema,10f,20f,30f) );
		signal.set_command(command);
		assertEquals(phase.getActualredcleartime(),30,1e-4);
		assertEquals(phase.getActualyellowtime(),20,1e-4);
	}

}
