package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.BeatsException;

@SuppressWarnings("serial")
final public class ScenarioValidationError extends BeatsException {
	public ScenarioValidationError() {
		super("Scenario validation failed. See error log for details");
	}
}
