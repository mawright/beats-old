package edu.berkeley.path.beats.simulator;

public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {

    protected Controller myController;
    public enum Implementation {beats,aimsun};
    protected ActuatorImplementation implementor;
    protected Actuator.Type myType;

	public static enum Type	{ ramp_meter,signal,vsl,cms };

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Actuator (){
	}
	
	public Actuator (Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,ActuatorImplementation act_implementor){

        this.implementor = act_implementor;
        this.myType = Actuator.Type.valueOf(jaxbA.getActuatorType().getName());

        // copy jaxb data
        setId(jaxbA.getId());
        //setScenarioElement(new ScenarioElement(myScenario,jaxbA.getScenarioElement()));
        setParameters(jaxbA.getParameters());
        //setActuatorType(jaxbA.getActuatorType());
        setTable(jaxbA.getTable());
	}

    public Actuator (ActuatorImplementation act_implementor,Actuator.Type myType){
        this.implementor = act_implementor;
        this.myType = myType;
    }

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

    protected boolean register(){
        switch(myType){
            case ramp_meter:
                return ((Link)implementor.target).register_flow_controller();
            case vsl:
                return ((Link)implementor.target).register_speed_controller();
            case signal:
                return false;
            case cms:
                return ((Node)implementor.target).register_split_controller();
        }
        return false;
    }

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

    protected void update() {
    }

	protected void deploy(double current_time_in_seconds){
    };

}
