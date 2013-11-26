package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.actuator.StageSplit;

import java.util.List;

public class BeatsActuatorImplementation implements InterfaceActuator {

	private Object target;      // Link or Node

	public BeatsActuatorImplementation(edu.berkeley.path.beats.jaxb.Actuator parent,Object context){

        Scenario scenario = (Scenario) context;
        edu.berkeley.path.beats.jaxb.ScenarioElement se = parent.getScenarioElement();

        switch(Actuator.Type.valueOf(parent.getActuatorType().getName())){
            case ramp_meter:
            case vsl:
                if(se.getType().compareTo("link")==0)
                    target = scenario.getLinkWithId(se.getId());
                break;
            case signalized_intersection:
            case cms:
                if(se.getType().compareTo("node")==0)
                    target = scenario.getNodeWithId(se.getId());
                break;
        }
	}
	
	@Override
	public void deploy_metering_rate_in_vph(Double metering_rate_in_vph) {
        ((Link)target).set_external_max_flow_in_vph(metering_rate_in_vph);
	}

	@Override
	public void deploy_stage_splits(StageSplit[] stage_splits) {




        for(StageSplit ss:stage_splits)
            System.out.print(ss.split + "\t");
        System.out.print("\n");
	}

	@Override
	public void deploy_cms_split() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void deploy_vsl_speed() {
		// TODO Auto-generated method stub
	}


}
