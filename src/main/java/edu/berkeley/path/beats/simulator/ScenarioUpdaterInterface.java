package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.BeatsException;

/**
 * Created by gomes on 10/26/14.
 */
public interface ScenarioUpdaterInterface {
    void populate();

    void update() throws BeatsException;
 }
