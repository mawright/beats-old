package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorRampMeter extends Actuator {

    public enum QueueOverrideStrategy {none,max_rate,proportional,proportional_integral};
    private double metering_rate_in_veh;
    private Link myLink;
	private double max_rate_in_veh;
	private double min_rate_in_veh;
    private QueueOverride queue_override;

    /////////////////////////////////////////////////////////////////////
    // actuation command
    /////////////////////////////////////////////////////////////////////

    public void setMeteringRateInVeh(Double rate_in_veh){
        metering_rate_in_veh = rate_in_veh;
        metering_rate_in_veh = Math.max(metering_rate_in_veh,min_rate_in_veh);
        metering_rate_in_veh = Math.min(metering_rate_in_veh,max_rate_in_veh);

        System.out.println(metering_rate_in_veh);
	}

	public void setMeteringRateInVPH(Double rate_in_vph){
        double dt_in_hours = myController.getMyScenario().getSimdtinseconds()/3600d;
        metering_rate_in_veh = rate_in_vph*dt_in_hours;
        metering_rate_in_veh = Math.max(metering_rate_in_veh,min_rate_in_veh);
        metering_rate_in_veh = Math.min(metering_rate_in_veh,max_rate_in_veh);
	}
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

    public ActuatorRampMeter(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,ActuatorImplementation act_implementor){
        super(myScenario,jaxbA,act_implementor);
    }

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxb,Scenario myScenario) {

        edu.berkeley.path.beats.jaxb.Actuator jaxbA = (edu.berkeley.path.beats.jaxb.Actuator) jaxb;
        double max_rate_in_vph = Double.POSITIVE_INFINITY;
        double min_rate_in_vph = 0d;
		myLink = myScenario.getLinkWithId(jaxbA.getScenarioElement().getId());

		if(myLink!=null && jaxbA.getParameters()!=null){
			double lanes = myLink.get_Lanes();
			for(Parameter p : jaxbA.getParameters().getParameter()){
				if(p.getName().compareTo("max_rate_in_vphpl")==0)
					max_rate_in_vph = Double.parseDouble(p.getValue())*lanes;
				if(p.getName().compareTo("min_rate_in_vphpl")==0)
					min_rate_in_vph = Double.parseDouble(p.getValue())*lanes;
			}	
		}

        if(jaxbA.getParameters()!=null){
            for(Parameter p : jaxbA.getParameters().getParameter()){
                if(p.getName().compareTo("max_rate_in_vphpl")==0)
                    max_rate_in_vph = Double.parseDouble(p.getValue())*myLink.get_Lanes();
                if(p.getName().compareTo("min_rate_in_vphpl")==0)
                    min_rate_in_vph = Double.parseDouble(p.getValue())*myLink.get_Lanes();
            }
        }

        double dt_in_hours = myScenario.getSimdtinseconds()/3600d;
        this.max_rate_in_veh = max_rate_in_vph*dt_in_hours;
        this.min_rate_in_veh = min_rate_in_vph*dt_in_hours;

        // queue override
        if(jaxbA.getQueueOverride()!=null)
            queue_override = new QueueOverride(jaxbA.getQueueOverride());
	}

	@Override
	protected void validate() {
		if(myLink==null)
			BeatsErrorLog.addError("Bad link ID in ramp metering actuator ID="+getId());
		if(max_rate_in_veh<0)
			BeatsErrorLog.addError("Negative max rate in ramp metering actuator ID="+getId());
		if(min_rate_in_veh<0)
			BeatsErrorLog.addError("Negative min rate in ramp metering actuator ID="+getId());
		if(max_rate_in_veh<min_rate_in_veh)
			BeatsErrorLog.addError("max rate less than min rate in actuator ID="+getId());
	}

	@Override
	public void deploy(double current_time_in_seconds) {
        if(queue_override!=null)
            metering_rate_in_veh = Math.max(metering_rate_in_veh,queue_override.compute_rate_in_veh());

        System.out.println(metering_rate_in_veh);

        this.implementor.deploy_metering_rate_in_veh(metering_rate_in_veh);
	}

    @Override
    protected boolean register() {
        return ((Link)implementor.get_target()).register_flow_controller();
    }
	
	public Link getLink(){
		return myLink;
	}


    public class QueueOverride {

        public QueueOverrideStrategy strategy;
        public double max_vehicles;

        public QueueOverride(edu.berkeley.path.beats.jaxb.QueueOverride jaxbQ){
            this.strategy = QueueOverrideStrategy.valueOf(jaxbQ.getStrategy());
            this.max_vehicles = jaxbQ.getMaxQueueVehicles();
        }

        public double compute_rate_in_veh(){

            double current_veh = myLink.getTotalDensityInVeh(0);

            switch(strategy){
                case none:
                    return Double.NEGATIVE_INFINITY;
                case max_rate:
                    if(BeatsMath.greaterorequalthan(current_veh,max_vehicles))
                        return max_rate_in_veh;
                    else
                        return Double.NEGATIVE_INFINITY;
                case proportional:
                case proportional_integral:
                    System.err.println("Not implemented.");
                    return Double.NaN;
                default:
                    return Double.NaN;
            }
        }

    }

}
