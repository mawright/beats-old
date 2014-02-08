package edu.berkeley.path.beats.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by gomes on 2/7/14.
 */
public class BeatsProperties extends Properties {

    public BeatsProperties(String prop_file_name){

        // REQUIRED
        this.setProperty("SCENARIO","");
        this.setProperty("SIM_DT","");
        this.setProperty("OUTPUT_PREFIX","");

        // Defaults
        this.setProperty("OUTPUT_FORMAT","text");
        this.setProperty("START_TIME","0");
        this.setProperty("DURATION","86540");
        this.setProperty("OUTPUT_DT","300");
        this.setProperty("NUM_REPS","1");
        this.setProperty("UNCERTAINTY_MODEL","gaussian");
        this.setProperty("NODE_FLOW_SOLVER","proportional");
        this.setProperty("NODE_SPLIT_RATIO_SOLVER","A");
        this.setProperty("RUN_MODE","normal");

        // load properties file
        try{
            load(new FileInputStream(prop_file_name));
        }
        catch (IOException e){
            System.err.print(e);
        }

        if(((String)get("SCENARIO")).isEmpty())
            System.out.println("NEED SCENARIO");
    }

}
