package edu.berkeley.path.beats.test.simulator;

import edu.berkeley.path.beats.Jaxb;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Scenario;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Created by gomes on 10/24/14.
 */
public class TestACTM {

    private String config = "C:\\Users\\gomes\\Dropbox\\_work_dynamic\\test_actm\\210W_pm_cropped_L0.xml";

    @Test
    @Ignore
    public void test_actm() {
        try {
            Scenario scenario = Jaxb.create_scenario_from_xml(config);

            double timestep = 5d;
            double starttime = 0d;
            double endtime = 18000;
            double outdt = 300d;
            String outtype = "text";
            String outprefix = "C:\\Users\\gomes\\Dropbox\\_work_dynamic\\test_actm\\actm";
            String uncertaintymodel = "gaussian";
            String nodeflowsolver = "proportional";
            String nodesrsolver = "A";
            String run_mode = "normal";

            boolean is_actm = true;

            scenario.initialize(timestep,starttime,endtime,outdt,outtype,outprefix,1,1,
                                uncertaintymodel, nodeflowsolver, nodesrsolver,"",run_mode,
                                "",Double.NaN ,null,is_actm);

            scenario.run();
        } catch (BeatsException e) {
            fail("initialization failure.");
        }
    }
}
