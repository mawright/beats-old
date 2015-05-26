package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.simulator.Clock;

import java.util.ArrayList;

public class BeatsTimeProfileDouble3D extends BeatsTimeProfile<Double[][][]> {

    private Double[][][] current_sample;        // sample per [vehicle type]
    private int nIn;
    private int nOut;
    private int nVt;

    public BeatsTimeProfileDouble3D(
            ArrayList<Integer> in_index,
            ArrayList<Integer> out_index,
            ArrayList<Integer> vt_index,
            ArrayList<String> splitString,
            int nIn,
            int nOut,
            int nVt,
            Double dt,
            Double startTime,
            double simdtinseconds) throws BeatsException {

        super(dt, startTime, simdtinseconds);

        this.nIn = nIn;
        this.nOut = nOut;
        this.nVt = nVt;

        // organize into array of SplitInfo, collect max length
        ArrayList<SplitInfo> splits = new ArrayList<SplitInfo>();
        int max_length = -1;
        for(int i=0;i<in_index.size();i++) {
            SplitInfo s = new SplitInfo(in_index.get(i), out_index.get(i), vt_index.get(i), splitString.get(i));
            splits.add(s);
            max_length = Math.max(max_length,s.get_length());
        }

        // populate data with nans
        int t;
        data = new ArrayList<Double [][][]>();
        for(t=0;t<max_length;t++)
            data.add(BeatsMath.nans(nIn, nOut, nVt));

        // copy in numbers
        for(t=0;t<data.size();t++) {
            Double[][][] z = data.get(t);
            for (SplitInfo splitinfo : splits) {
                int in = splitinfo.inIndex;
                int out = splitinfo.outIndex;
                int vt = splitinfo.vtIndex;
                z[in][out][vt] = splitinfo.X==null ? Double.NaN : splitinfo.X[t];
            }
            data.set(t, z);
        }
    }

    public void validate(double simdt,double minval,double maxval) {
//        super.validate(simdt);
//
//        // check values are in range
//        int i,j,k;
//        for(Double[][][] X : data)
//            for (i=0; i<X.length; i++)
//                for (j = 0; j < X[i].length; j++)
//                    for (k = 0; k < X[i][j].length; k++)
//                        if (BeatsMath.greaterthan(X[i][j][k],maxval) | BeatsMath.lessthan(X[i][j][k],minval))
//                            BeatsErrorLog.addError("Values out of range.");
    }

    // returns true iff a new sample was chosen
    public boolean sample(Clock clock,boolean forcesample){

        if( forcesample | clock.is_time_to_sample_abs(samplesteps, step_initial_abs)){

            // REMOVE THESE
            int n = data.size()-1;
            int step = clock.sample_index_abs(samplesteps,step_initial_abs);

            // dont sample before start time
            if(clock.getAbsoluteTimeStep()< step_initial_abs)
                return false | forcesample;

            // sample the profile
            if(step<n){
                current_sample = BeatsMath.normalize(data.get(step));
                return true;
            }

            // last sample
            if(step>=n && !isdone){
                current_sample = BeatsMath.normalize(data.get(data.size()-1));
                isdone = true;
                return true;
            }

            // sample CPs
//            currentConcentrationParameters = sampleConcentrationParametersAtTimeStep( Math.min( step, laststep-1) );

//            // assign
//            myNode.setSampledSRProfile(currentSplitRatio);

        }

        return false;
    }

    public Double [][][] getFirst(){
        return data.get(0);
    }

    public Double [][][] getCurrentSample(){
        return current_sample;
    }

    // for time sample k, returns a 3D matrix with dimensions inlink x outlink x vehicle type
//    private Double [][][] sampleAtTimeStep(int k){
//        return data.get( Math.min(k,data.size()-1) );
//    }

//    private Double3DMatrix sampleConcentrationParametersAtTimeStep(int k){
//        if (!hasConcentrationParameters())
//            return null;
//        Double3DMatrix X = new Double3DMatrix(myNode.getnIn(),myNode.getnOut(),
//                myScenario.get.numVehicleTypes(),Double.NaN);	// initialize all unknown
//
//        int i,j,v,lastk;
//        for(i=0;i<myNode.getnIn();i++){
//            for(j=0;j<myNode.getnOut();j++){
//                for(v=0;v<myScenario.get.numVehicleTypes();v++){
//                    if(concentrationParamsProfile[i][j][v]==null)						// nan if not defined
//                        continue;
//                    if(concentrationParamsProfile[i][j][v].isEmpty())					// nan if no data
//                        continue;
//                    lastk = Math.min(k,concentrationParamsProfile[i][j][v].getNumTime()-1);	// hold last value
//                    X.set(i,j,v,concentrationParamsProfile[i][j][v].get(lastk));
//                }
//            }
//        }
//        return X;
//    }

    class SplitInfo {
        int inIndex;
        int outIndex;
        int vtIndex;
        Double [] X;
        public SplitInfo(int inIndex,int outIndex,int vtIndex,String str){
            this.inIndex = inIndex;
            this.outIndex = outIndex;
            this.vtIndex = vtIndex;
            this.X = BeatsFormatter.readCSVstring_nonnegative(str, ",");
        }
        public int get_length(){
            return X==null ? 0 : X.length;
        }
    }

}