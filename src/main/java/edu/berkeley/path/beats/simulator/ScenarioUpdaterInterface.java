package edu.berkeley.path.beats.simulator;

/**
 * Created by gomes on 10/26/14.
 */
public interface ScenarioUpdaterInterface {
    void populate();
    void update() throws BeatsException;
 }
