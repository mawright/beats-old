package edu.berkeley.path.beats.simulator;

public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {

    protected Controller myController;
    public enum Implementation {beats,aimsun};
    protected ActuatorImplementation implementor;

	public static enum Type	{ ramp_meter,
							  signalized_intersection,
							  vsl,
							  cms };

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Actuator (){
	}
	
	public Actuator (Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,ActuatorImplementation act_implementor){

        this.implementor = act_implementor;

        // copy jaxb data
        setId(jaxbA.getId());
        setScenarioElement(new ScenarioElement(myScenario,jaxbA.getScenarioElement()));
        setParameters(jaxbA.getParameters());
        setActuatorType(jaxbA.getActuatorType());
        setTable(jaxbA.getTable());
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject,Scenario myScenario) {
		return;
	}

	protected void validate() {
//		if(implementor.getLink()==null)
//			BeatsErrorLog.addError("Bad link reference in actuator id="+getId());
	}

	protected void reset() throws BeatsException {
		return;
	}
	
	protected void deploy(double current_time_in_seconds){
    };

//    public long getId() {
//        return jaxbA.getId();
//    }

//    public ActuatorImplementation get_implementor(){
//        return implementor;
//    }

//    public String getScenarioElementType() {
//        return jaxbA.getScenarioElement().getType();
//    }
//
//    public long getScenarioElementId() {
//        return jaxbA.getScenarioElement().getId();
//    }

//    public Signal getSignal(){
//        ScenarioElement se = (ScenarioElement) getScenarioElement();
//        if(se.getType().compareTo("signal")==0)
//            return (Signal) se.getReference();
//        else
//            return null;
//    }

}
