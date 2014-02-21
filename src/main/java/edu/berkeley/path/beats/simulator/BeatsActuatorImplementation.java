package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.actuator.ActuatorSignalStageSplits;
import edu.berkeley.path.beats.actuator.StageSplit;
import edu.berkeley.path.beats.jaxb.Splitratio;

import java.util.List;

public class BeatsActuatorImplementation extends ActuatorImplementation {

	public BeatsActuatorImplementation(edu.berkeley.path.beats.jaxb.Actuator parent,Object context){

        Scenario scenario = (Scenario) context;
        edu.berkeley.path.beats.jaxb.ScenarioElement se = parent.getScenarioElement();

        switch(Actuator.Type.valueOf(parent.getActuatorType().getName())){
            case ramp_meter:
            case vsl:
                target = scenario.getLinkWithId(se.getId());
                break;
            case cms:
                target = scenario.getNodeWithId(se.getId());
                break;
        }
	}

    public BeatsActuatorImplementation(edu.berkeley.path.beats.jaxb.Signal parent,Object context){
        Scenario scenario = (Scenario) context;
        target = scenario.getNodeWithId(parent.getNodeId());
    }
	
	@Override
	public void deploy_metering_rate_in_veh(Double metering_rate_in_veh) {
        ((Link)target).set_external_max_flow_in_veh(metering_rate_in_veh);
	}

	@Override
	public void deploy_stage_splits(StageSplit[] stage_splits) {

        // normalize splits
        double sum_splits = 0d;
        for(StageSplit stage_split : stage_splits)
            sum_splits += stage_split.split;
        if(BeatsMath.greaterthan(sum_splits,0d))
            for(StageSplit stage_split : stage_splits)
                stage_split.split /= sum_splits;
        else{
            for(StageSplit stage_split : stage_splits)
                stage_split.split = 0d;
            stage_splits[0].split = 1d;
        }

        // map splits onto links
        ActuatorSignalStageSplits myAct =(ActuatorSignalStageSplits) myActuator;
        double [] link_splits = BeatsMath.zeros(myAct.inlinks.size());
        for(StageSplit stage_split:stage_splits){
            if(stage_split.stage.phaseA!=null)
                for(Link link : stage_split.stage.phaseA.getTargetlinks())
                    link_splits[myAct.inlinks.indexOf(link)] += stage_split.split;
            if(stage_split.stage.phaseB!=null)
                for(Link link : stage_split.stage.phaseB.getTargetlinks())
                    link_splits[myAct.inlinks.indexOf(link)] += stage_split.split;
        }

        // send capacities to links
        for(int i=0;i<myAct.inlinks.size();i++){
            Link link = myAct.inlinks.get(i);
            link.set_external_max_flow_in_vph(link.getCapacityInVPS(0)*3600d*link_splits[i]);
        }

	}

	@Override
	public void deploy_cms_split(List<Splitratio> splits) {
		if(target==null)
            return;
        ((Node)target).set_controller_split(splits);
	}

	@Override
	public void deploy_vsl_speed() {
		// TODO Auto-generated method stub
	}


    private class LinkSplit {
        public Link link;
        double split;
        public LinkSplit(Link link){
            this.link = link;
            this.split = 0d;
        }

    }

}
