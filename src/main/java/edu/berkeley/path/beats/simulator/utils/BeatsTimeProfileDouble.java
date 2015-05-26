package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.simulator.Clock;
import edu.berkeley.path.beats.simulator.Scenario;

import java.util.ArrayList;

public class BeatsTimeProfileDouble extends BeatsTimeProfile {

    protected double dtinseconds;            // not really necessary
    protected int samplesteps;
    protected int step_initial_abs;       // time steps at start since midnight
    protected boolean isdone;
    protected double current_sample;

    // initialize a 1D vector from comma separated string of positive numbers
    // negative numbers get replaced with nan.
    public BeatsTimeProfileDouble(String str, String delim, Double dt, Double startTime, double simdtinseconds) {
//        data = dataFromDoubles(BeatsFormatter.readCSVstring_nonnegative(str, delim));
        data = BeatsFormatter.readCSVstring_nonnegative(str, delim);

        ///////////////////////////////
        isdone = false;

        // optional dt
        if (dt != null) {
            dtinseconds = dt.floatValue();                    // assume given in seconds
            samplesteps = BeatsMath.round(dtinseconds / simdtinseconds);
        } else {    // allow only if it contains one time step
            if (data.length == 1) {
                dtinseconds = Double.POSITIVE_INFINITY;
                samplesteps = Integer.MAX_VALUE;
            } else {
                dtinseconds = -1.0;        // this triggers the validation error
                samplesteps = -1;
            }
        }

        // step_initial
        double start_time = Double.isInfinite(startTime) ? 0d : startTime;
        step_initial_abs = BeatsMath.round(start_time / simdtinseconds);
        ///////////////////////////////

    }

//    public BeatsTimeProfileDouble(String str, boolean allownegative) {
//        data = dataFromDoubles(
//                allownegative ?
//                        BeatsFormatter.readCSVstring(str, ",") :
//                        BeatsFormatter.readCSVstring_nonnegative(str, ",") );
//        isdone = false;
//    }
//
//    public BeatsTimeProfileDouble(String str, boolean allownegative, Scenario scenario) {
//        data = dataFromDoubles(
//                allownegative ?
//                        BeatsFormatter.readCSVstring(str, ",") :
//                        BeatsFormatter.readCSVstring_nonnegative(str, ",") );
//        this.sampleSteps = (int) (dt/scenario.get.simdtinseconds());
//        isdone = false;
//    }

    public void reset() {
        isdone = false;
        current_sample = data[0];
    }

    public double getDtinseconds() {
        return dtinseconds;
    }

    public boolean isDone() {
        return isdone;
    }

    public void addscalar(double value) {
        if (data == null)
            return;
        int i;
        for (i = 0; i < data.length; i++)
            data[i] += value;
    }

    public Double getCurrentSample() {
        return current_sample;
    }



//    public void copydata(BeatsTimeProfileScalar in){
//    	if(data==null)
//    		return;
//    	if(in.data.size()!=data.size())
//    		return;
//    	int i;
//    	for(i=0;i<data.size();i++)
//    		data.set(i,in.data.get(i));
//    }

//    private Double [] dataFromDoubles(Double [] X){
//        Double [] z = new ArrayList<Double>();
//        for(Double x : X)
//            z.add(x);
//        return z;
//    }

    public Double sample(boolean forcesample,Clock clock){

        if(!isdone && (forcesample || clock.is_time_to_sample_abs(samplesteps, step_initial_abs))){

            int n = data.length-1;
            int step = clock.sample_index_abs(samplesteps,step_initial_abs);

            // zeroth sample extends to the left
            if(step<=0) {
                current_sample = data[0];
                return current_sample;
            }

            // sample the profile
            if(step<n) {
                current_sample = data[step];
                return current_sample;
            }

            // last sample
            if(step>=n && !isdone){
                isdone = true;
                current_sample = data[n];
                return current_sample;
            }
        }

        return current_sample;
    }


}