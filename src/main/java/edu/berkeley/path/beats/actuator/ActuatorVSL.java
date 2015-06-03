package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorVSL extends Actuator {

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

//	public ActuatorVSL(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
//		super(myScenario,jaxbA);
//	}

    @Override
    public boolean register() {
        return ((Link)implementor.get_target()).register_speed_controller();
    }
}
