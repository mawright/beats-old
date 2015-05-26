package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.simulator.Scenario;

import java.util.ArrayList;

public class BeatsTimeProfile <T> {

    protected double dtinseconds;            // not really necessary
    protected int samplesteps;
    protected int step_initial_abs;       // time steps at start since midnight
    protected boolean isdone;
	protected ArrayList<T> data;
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

    public BeatsTimeProfile(){}


    public BeatsTimeProfile(Double dt,Double startTime, double simdtinseconds){

        isdone = false;

        // step_initial
        double start_time = Double.isInfinite(startTime) ? 0d : startTime;
        step_initial_abs = BeatsMath.round(start_time/simdtinseconds);

        // dt
        dtinseconds = dt!=null ? dt.floatValue() : Double.POSITIVE_INFINITY; // assume given in seconds
        samplesteps = BeatsMath.round(dtinseconds/simdtinseconds);
    }


//    public BeatsTimeProfile(int n,Double val) {
//        data = new Double [n];
//        for (int i = 0; i < n; i++)
//            data[i] = val;
//    }
//
//    public BeatsTimeProfile(String str,boolean allownegative) {
//        if(allownegative)
//            data = BeatsFormatter.readCSVstring(str, ",");
//        else
//            data = BeatsFormatter.readCSVstring_nonnegative(str, ",");
//    }
//
//    public BeatsTimeProfile(String str,boolean allownegative,Scenario scenario) {
//        if(allownegative)
//    	    data = BeatsFormatter.readCSVstring(str, ",");
//        else
//            data = BeatsFormatter.readCSVstring_nonnegative(str, ",");
//        this.sampleSteps = (int) (dt/scenario.get.simdtinseconds());
//    }

//    // initialize a 1D vector from comma separated string of positive numbers
//    // negative numbers get replaced with nan.
//    public BeatsTimeProfile(String str,String delim) {
//    	data = BeatsFormatter.readCSVstring_nonnegative(str, delim);
//    }
    
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  


    public void reset(){
        isdone = false;
    }

    public double getDtinseconds() {
        return dtinseconds;
    }

    public boolean isDone() {
        return isdone;
    }

    public boolean isEmpty() {
        return data==null ? true : data.size()==0;
    }

    public Integer getNumTime() {
        return data==null ? 0 : data.size();
    }

    public T get(int i){
        if(data==null)
            return null;
        if(data.size()==0)
            return null;
        return data.get(i);
    }

    public int getSampleSteps(){
        return samplesteps;
    }

	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
    public void set(int i,T f){
    	if(data!=null)
    		data.set(i,f);
    }

//    public void multiplyscalar(double value){
//    	if(data==null)
//    		return;
//    	int i;
//    	for(i=0;i<data.length;i++)
//    		data[i] *= value;
//    }
//
//    public void addscalar(double value){
//    	if(data==null)
//    		return;
//    	int i;
//    	for(i=0;i<data.length;i++)
//    		data[i] += value;
//    }
//
//    public void copydata(BeatsTimeProfile in){
//    	if(data==null)
//    		return;
//    	if(in.data.length!=data.length)
//    		return;
//    	int i;
//    	for(i=0;i<data.length;i++)
//    		data[i] = in.data[i];
//    }
    
	/////////////////////////////////////////////////////////////////////
	// check data
	/////////////////////////////////////////////////////////////////////  

    public boolean hasNaN(){
        if(data==null)
            return false;
        for(int i=0;i<data.size();i++)
            if(data.get(i)==null)
                return true;
        return false;
    }

//
//    public void multiplyscalar(double value) {
//        if (data == null)
//            return;
//        int i;
//        for (i = 0; i < data.length; i++)
//            data[i] *= value;
//    }


}
