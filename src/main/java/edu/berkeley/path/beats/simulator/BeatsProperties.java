package edu.berkeley.path.beats.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by gomes on 2/7/14.
 */
public class BeatsProperties extends Properties {

    String scenario_name;
    String performance_config;
    Double sim_dt;
    String output_prefix;
    String output_format;
    Double start_time;
    Double duration;
    Double output_dt;
    Integer num_reps;
    String uncertainty_model;
    String split_ratio_model;
    String node_flow_model;
    String run_mode;
    Integer ensemble_size;

    public BeatsProperties(String prop_file_name) throws BeatsException {

        // load properties file
        try{
            load(new FileInputStream(prop_file_name));
        }
        catch (IOException e){
            System.err.print(e);
        }

        // read properties
        scenario_name = getProperty("SCENARIO","");
        sim_dt = getProperty("SIM_DT")==null ? Double.NaN : Double.parseDouble(getProperty("SIM_DT"));
        output_prefix = getProperty("OUTPUT_PREFIX","");
        output_format = getProperty("OUTPUT_FORMAT", "text");
        start_time = Double.parseDouble(getProperty("START_TIME","0"));
        duration = Double.parseDouble(getProperty("DURATION","86400"));
        output_dt = Double.parseDouble(getProperty("OUTPUT_DT","300"));
        num_reps = Integer.parseInt(getProperty("NUM_REPS","1"));
        uncertainty_model = getProperty("UNCERTAINTY_MODEL", "gaussian");
        split_ratio_model = getProperty("NODE_SPLIT_RATIO_SOLVER","A");
        node_flow_model = getProperty("NODE_FLOW_SOLVER","proportional");
        run_mode = getProperty("RUN_MODE","normal");
        ensemble_size = Integer.parseInt(getProperty("ENSEMBLE_SIZE","1"));
        performance_config = getProperty("PERFORMANCE","");

        // validate
        if(scenario_name.isEmpty())
            throw new BeatsException("Scenario name not provided in properties file.");
        if(Double.isNaN(sim_dt))
            throw new BeatsException("Simulation dt not provided in properties file.");
        if(output_prefix.isEmpty())
            throw new BeatsException("Output prefix not provided in properties file.");

    }


    @Override
    public synchronized String toString() {
        String str;
        str = "SCENARIO = " + scenario_name +"\n";
        str += "SIM_DT = " + sim_dt +"\n";
        str += "OUTPUT_PREFIX = " +output_prefix + "\n";
        str += "OUTPUT_FORMAT = " +output_format + "\n";
        str += "START_TIME = " + start_time+ "\n";
        str += "DURATION = " + duration+ "\n";
        str += "OUTPUT_DT = " + output_dt+ "\n";
        str += "NUM_REPS = " + num_reps+ "\n";
        str += "UNCERTAINTY_MODEL = " + uncertainty_model + "\n";
        str += "NODE_FLOW_SOLVER = " + node_flow_model + "\n";
        str += "NODE_SPLIT_RATIO_SOLVER = " + split_ratio_model + "\n";
        str += "RUN_MODE = " + run_mode + "\n";
        str += "ENSEMBLE_SIZE = " + ensemble_size + "\n";
        str += "PERFORMANCE = " + performance_config;
        return str;
    }

}