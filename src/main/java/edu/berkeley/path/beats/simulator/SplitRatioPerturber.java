package edu.berkeley.path.beats.simulator;

public class SplitRatioPerturber {

	/**
	 * @param args
	 */
	private SplitRatioPerturber() {
		// TODO Auto-generated method stub
	}
	
	public static Double3DMatrix perturb2OutputSplit(Double3DMatrix split, double variance){
		if(split.getnIn()!=1 && split.getnOut()!=2)
			return split;
		
		Double3DMatrix splitPerturbed = new Double3DMatrix(split);
		
		for (int v=0;v<split.getnVTypes();v++){
			double max = 0;
			int max_index = 0;
			for (int o=0;o<split.getnOut();o++){
				if (max<split.get(0, o, v)){
					max=split.get(0, o, v);
					max_index = o;
				}
			}
			double[] params = BeatsMath.betaParamsFromRVMeanAndVariance(max, variance);
			double[] sample = BeatsMath.sampleDirichlet(params);
			splitPerturbed.set(0, max_index, v, sample[0]);
			splitPerturbed.set(0, 1-max_index, v, sample[1]);
		}
		return splitPerturbed;
	}

}
