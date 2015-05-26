package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.simulator.Scenario;

import java.util.ArrayList;

public class BeatsTimeProfileDemands {

    protected Double [] data;
    protected double dt = 300d;  // time step in seconds (TEMPORARY)
    protected int sampleSteps;

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public BeatsTimeProfileDemands(){}

    public BeatsTimeProfileDemands(int n,Double val) {
        data = BeatsMath.zeros(n);
        for (int i = 0; i < n; i++)
            data[i] = val;
    }

    public BeatsTimeProfileDemands(String str,boolean allownegative) {
        if(allownegative)
            data = BeatsFormatter.readCSVstring(str, ",");
        else
            data = BeatsFormatter.readCSVstring_nonnegative(str, ",");
    }

    public BeatsTimeProfileDemands(String str,boolean allownegative,Scenario scenario) {
        if(allownegative)
            data = BeatsFormatter.readCSVstring(str, ",");
        else
            data = BeatsFormatter.readCSVstring_nonnegative(str, ",");
        this.sampleSteps = (int) (dt/scenario.get.simdtinseconds());
    }

    // initialize a 1D vector from comma separated string of positive numbers
    // negative numbers get replaced with nan.
    public BeatsTimeProfileDemands(String str,String delim) {
        data = BeatsFormatter.readCSVstring_nonnegative(str, delim);
    }

    /////////////////////////////////////////////////////////////////////
    // public interface
    /////////////////////////////////////////////////////////////////////

    public boolean isEmpty() {
        if(data==null)
            return true;
        return data.length==0;
    }

    public Integer getNumTime() {
        if(data==null)
            return 0;
        return data.length;
    }

    public Double [] getData(){
        return data;
    }

    public Double get(int i){
        if(data==null)
            return null;
        if(data.length==0)
            return null;
        return data[i];
    }

    public int getSampleSteps(){
        return sampleSteps;
    }

    /////////////////////////////////////////////////////////////////////
    // alter data
    /////////////////////////////////////////////////////////////////////

//    public void set(int i,double f){
//    	if(data!=null)
//    		data[i] = f;
//    }
//
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
        for(int i=0;i<data.length;i++) {
            if (Double.isNaN((Double) data[i]))
                return true;
        }
        return false;
    }


    public void multiplyscalar(double value) {
        if (data == null)
            return;
        int i;
        for (i = 0; i < data.length; i++)
            data[i] *= value;
    }


}
