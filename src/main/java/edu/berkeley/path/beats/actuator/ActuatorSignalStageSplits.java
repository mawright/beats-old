package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.InterfaceActuator;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorSignalStageSplits extends Actuator {

    private StageSplit [] stage_splits;

    /////////////////////////////////////////////////////////////////////
    // actuation command
    /////////////////////////////////////////////////////////////////////

    public void setStageSplits(StageSplit [] stage_splits){
		this.stage_splits = stage_splits;
	}

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
//	public ActuatorSignalGreenSplits(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
//		super(myScenario,jaxbA);
//	}

    public ActuatorSignalStageSplits(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,InterfaceActuator act_implementor){
        super(myScenario,jaxbA,act_implementor);
    }

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

//	@Override
//	protected void populate(Object jaxbobject) {
//		return;
//	}
//
//	@Override
//	protected void validate() {
//	}
//
//	@Override
//	protected void reset() throws BeatsException {
//		return;
//	}

	@Override
	public void deploy(double current_time_in_seconds) {
		this.implementor.deploy_stage_splits(stage_splits);
	}

}
