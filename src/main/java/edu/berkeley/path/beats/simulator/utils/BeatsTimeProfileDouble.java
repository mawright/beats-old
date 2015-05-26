package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.simulator.Clock;

import java.util.ArrayList;

public class BeatsTimeProfileDouble extends BeatsTimeProfile<Double> {

    protected Double current_sample;

    // initialize a 1D vector from comma separated string of positive numbers
    // negative numbers get replaced with nan.
    public BeatsTimeProfileDouble(String str, String delim, Double dt, Double startTime, double simdtinseconds) {
        super(dt,startTime,simdtinseconds);
        data = dataFromDoubles(BeatsFormatter.readCSVstring_nonnegative(str, delim));
    }

    public void reset() {
        isdone = false;
        current_sample = data.get(0);
    }

    public void multiplyscalar(double value) {
        if (data == null)
            return;
        int i;
        for (i = 0; i < data.size(); i++)
            data.set(i, data.get(i) * value);
    }

    public void addscalar(Double value) {
        if (data == null)
            return;
        int i;
        for (i = 0; i < data.size(); i++)
            data.set(i,(Double) data.get(i)+value);
    }

    public Double getCurrentSample() {
        return current_sample;
    }




    private ArrayList<Double> dataFromDoubles(Double [] X){
        ArrayList<Double> z = new ArrayList<Double>();
        for(Double x : X)
            z.add(x);
        return z;
    }

    public Double sample(boolean forcesample,Clock clock){

        if(!isdone && (forcesample || clock.is_time_to_sample_abs(samplesteps, step_initial_abs))){

            int n = data.size()-1;
            int step = clock.sample_index_abs(samplesteps,step_initial_abs);

            // zeroth sample extends to the left
            if(step<=0) {
                current_sample = data.get(0);
                return current_sample;
            }

            // sample the profile
            if(step<n) {
                current_sample = data.get(step);
                return current_sample;
            }

            // last sample
            if(step>=n && !isdone){
                isdone = true;
                current_sample = data.get(n);
                return current_sample;
            }
        }

        return current_sample;
    }


}