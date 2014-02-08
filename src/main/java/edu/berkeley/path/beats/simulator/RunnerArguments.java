package edu.berkeley.path.beats.simulator;

/**
 * The simulation settings
 */
final class RunnerArguments {
	
	// given
	public String configfilename;				// configuration file XML
    public Double simdt;						// [sec] simulation time step
    public String outputfileprefix;			// prefix for output files
    public String output_format;				// output format {text,xml,db}
    public Double startTime = null; 			// [sec] output start time
    public Double duration = null; 			// [sec] output duration
    public Double outputDt = null; 			// [sec] output period
    public Integer numReps = null;				// number of sequential runs
    public String uncertaintymodel;			// name of the uncertainty model
    public String nodeflowsolver;				// name of the node flow solver
    public String nodesrsolver;				// name of the node split ratio solver

    //public RunnerArguments parent = null;

//	public RunnerArguments(RunnerArguments parent) {
//		this.parent = parent;
//	}

	public RunnerArguments(String outputfileprefix,String output_format,Double startTime, Double duration, Double outputDt, Integer numReps,String uncertaintymodel,String nodeflowsolver,String nodesrsolver) {
		this.outputfileprefix = outputfileprefix;
		this.output_format = output_format;
		this.startTime = startTime;
		this.duration = duration;
		this.outputDt = outputDt;
		this.numReps = numReps;
		this.uncertaintymodel = uncertaintymodel;
		this.nodeflowsolver = nodeflowsolver;
		this.nodesrsolver = nodesrsolver;
	}

//	public RunnerArguments getParent() {
//		return parent;
//	}

	private double round(double val) {
		return BeatsMath.round(val * 10.0) / 10.0;
	}

	public void parseArgs(String[] args, int index) {
		if (index < args.length)
			this.configfilename = args[index];
		if (++index < args.length)
			this.simdt = Double.parseDouble(args[index]);
		if (++index < args.length)
			this.outputfileprefix = args[index];
		if (++index < args.length)
			this.output_format = args[index];
		if (++index < args.length) 
			startTime = round(Double.parseDouble(args[index]));
		if (++index < args.length)
			duration = Double.parseDouble(args[index]);
		if (++index < args.length) 
			outputDt = round(Double.parseDouble(args[index]));
		if (++index < args.length) 
			numReps = Integer.parseInt(args[index]);
		if (++index < args.length) 
			uncertaintymodel = args[index];
		if (++index < args.length) 
			nodeflowsolver = args[index];
		if (++index < args.length) 
			nodesrsolver = args[index];
	}

	public static RunnerArguments defaults() {
		return new RunnerArguments( "outputs",
									"xml",
									Double.valueOf(Defaults.TIME_INIT),
									Double.valueOf(Defaults.DURATION),
									Double.valueOf(Defaults.OUT_DT),
									1,
									"uniform",
									"proportional",
									"A");
	}

}
