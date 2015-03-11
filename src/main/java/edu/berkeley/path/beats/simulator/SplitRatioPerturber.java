package edu.berkeley.path.beats.simulator;

import java.util.Arrays;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;

public class SplitRatioPerturber {

	/**
	 * @param args
	 */
	private SplitRatioPerturber() {
		// TODO Auto-generated method stub
	}
	
	public static Double3DMatrix[] sampleFromConcentrationParameters( Double3DMatrix params, int numEnsemble){
		// Assumes concentration parameters of each input link are independent
		Double3DMatrix[] splitPerturbed = new Double3DMatrix[numEnsemble];
		
		int v,o,i,e;
		double[] paramsForInputLink = new double[params.getnOut()];
		double[][] sample;
		for (v=0;v<params.getnVTypes();v++){
			for (i=0;i<params.getnIn();i++){
				for (o=0;o<params.getnOut();o++){
					paramsForInputLink[o] = params.get(i, o, v) + 1;
				}
				sample = BeatsMath.sampleDirichlet(paramsForInputLink, numEnsemble);
				for (e=0;e<numEnsemble;e++){
					splitPerturbed[e] = new Double3DMatrix(params.getnIn(), params.getnOut(), params.getnVTypes(), 0d);
					for (o=0;o<params.getnOut();o++){
						splitPerturbed[e].set(i, o, v, sample[e][o]);
				}
				}
			}
		}
		
		return splitPerturbed;
	}
	
	public static Double3DMatrix[] perturb2OutputSplit(Double3DMatrix split, SplitRatioProfile profile, int numEnsemble){
		Double3DMatrix[] splitPerturbed = new Double3DMatrix[numEnsemble];
		
		if(split.getnIn()!=1 && split.getnOut()!=2){ // return non-perturbed if non 1 to 2 split given
			Arrays.fill(splitPerturbed, split);
			return splitPerturbed;
		}
		int e;
		for (e=0;e<numEnsemble;e++){
			splitPerturbed[e] = new Double3DMatrix(split);
		}
		
		for (int v=0;v<split.getnVTypes();v++){
			double max = 0;
			int max_index = 0;
			for (int o=0;o<split.getnOut();o++){
				if (max<split.get(0, o, v)){
					max=split.get(0, o, v);
					max_index = o;
				}
			}
            double[] params;
            if (profile.getSampleSize()!=null){
                params = BeatsMath.betaParamsFromRVModeAndSampleSize(max, profile.getSampleSize());
            }
            else {
    			params = BeatsMath.betaParamsFromRVMeanAndVariance(max, profile.getVariance()); }
			double[][] sample;
			try{
				sample = BeatsMath.sampleDirichlet(params, numEnsemble);
			}
			catch (NotStrictlyPositiveException ex){
				double newmax = max - profile.getVariance()*1.5; //
				params = BeatsMath.betaParamsFromRVMeanAndVariance(newmax, profile.getVariance());
				sample = BeatsMath.sampleDirichlet(params, numEnsemble);
			}
			for (e=0;e<numEnsemble;e++){
				splitPerturbed[e].set(0, max_index, v, sample[e][0]);
				splitPerturbed[e].set(0, 1-max_index, v, sample[e][1]);
			}
		}
		return splitPerturbed;
	}

}
