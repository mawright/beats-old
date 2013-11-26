package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.actuator.StageSplit;

public interface InterfaceActuator {
	public void deploy_metering_rate_in_vph(Double metering_rate);
	public void deploy_stage_splits(StageSplit[] stage_splits);
	public void deploy_cms_split();
	public void deploy_vsl_speed();
}
