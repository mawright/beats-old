/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator;

final public class DemandProfile extends edu.berkeley.path.beats.jaxb.DemandProfile {

	private Scenario myScenario;
	private double current_sample;
	private boolean isOrphan;
	private double dtinseconds;				// not really necessary
	private int samplesteps;				// [sim steps] profile sample period
	private BeatsTimeProfile demand_nominal;	// [veh]
	private boolean isdone; 
	private int stepinitial;
	private int vehicle_type_index;

	private double _knob;
	private Double std_dev_add;			// [veh]
	private Double std_dev_mult;			// [veh]
	private boolean isdeterministic;		// true if the profile is deterministic

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	protected void set_knob(double _knob) {
		this._knob = Math.max(_knob,0.0);
		
		// resample the profile
		update(true);
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		this.vehicle_type_index = myScenario.getVehicleTypeIndexForId(getVehicleTypeId());
		
		isdone = false;
		
		// required
		Link myLink = myScenario.getLinkWithId(getLinkIdOrigin());
		isOrphan = myLink==null;
		
		if(!isOrphan && vehicle_type_index>=0)
			myLink.setMyDemandProfile(this,vehicle_type_index);
		
		// sample demand distribution, convert to vehicle units
		if(!isOrphan && getContent()!=null){
			demand_nominal = new BeatsTimeProfile(getContent());
			demand_nominal.multiplyscalar(myScenario.getSimdtinseconds());
		}
		
		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = BeatsMath.round(dtinseconds/myScenario.getSimdtinseconds());
		}
		else{ 	// allow only if it contains one time step
			if(demand_nominal.getNumTime()==1){
				dtinseconds = Double.POSITIVE_INFINITY;
				samplesteps = Integer.MAX_VALUE;
			}
			else{
				dtinseconds = -1.0;		// this triggers the validation error
				samplesteps = -1;
				return;
			}
		}
		
		// optional uncertainty model
		if(getStdDevAdd()!=null)
			std_dev_add = getStdDevAdd().doubleValue() * myScenario.getSimdtinseconds();
		else
			std_dev_add = Double.POSITIVE_INFINITY;		// so that the other will always win the min
		
		if(getStdDevMult()!=null)
			std_dev_mult = getStdDevMult().doubleValue() * myScenario.getSimdtinseconds();
		else
			std_dev_mult = Double.POSITIVE_INFINITY;	// so that the other will always win the min
		
		isdeterministic = (getStdDevAdd()==null || std_dev_add==0.0) && 
						  (getStdDevMult()==null || std_dev_mult==0.0);
		
		_knob = getKnob().doubleValue();
		
	}

	protected void validate() {
		
		if(demand_nominal==null || demand_nominal.isEmpty())
			return;
		
		if(isOrphan)
			BeatsErrorLog.addWarning("Bad origin link id=" + getLinkIdOrigin() + " in demand profile.");
		
		if(vehicle_type_index<0)
			BeatsErrorLog.addError("Bad vehicle type id in demand profile for link id=" + getLinkIdOrigin());
		
		// check dtinseconds
		if( dtinseconds<=0 && demand_nominal.getNumTime()>1 )
			BeatsErrorLog.addError("Non-positive time step in demand profile for link id=" + getLinkIdOrigin());
		
		if(!BeatsMath.isintegermultipleof(dtinseconds,myScenario.getSimdtinseconds()) && demand_nominal.getNumTime()>1 )
			BeatsErrorLog.addError("Demand time step in demand profile for link id=" + getLinkIdOrigin() + " is not a multiple of simulation time step.");
		
		// check non-negative
		if(demand_nominal.hasNaN())
			BeatsErrorLog.addError("Illegal values in demand profile for link id=" + getLinkIdOrigin());

	}

	protected void reset() {
		
		if(isOrphan)
			return;
					
		isdone = false;
		
		// read start time, convert to stepinitial
		double starttime;	// [sec]
		if( getStartTime()!=null)
			starttime = getStartTime().floatValue();
		else
			starttime = 0f;

		stepinitial = BeatsMath.round((starttime-myScenario.getTimeStart())/myScenario.getSimdtinseconds());
		
		// set current sample to zero
		current_sample = 0d;
		
		// set knob back to its original value
		_knob = getKnob().doubleValue();	
	}
	
	protected void update(boolean forcesample) {
		
		if(isOrphan)
			return;
		
		if(demand_nominal==null || demand_nominal.isEmpty())
			return;
		
		if(isdone && !forcesample)
			return;
		
		if(forcesample || myScenario.getClock().istimetosample(samplesteps,stepinitial)){
			
			// REMOVE THESE
			int n = demand_nominal.getNumTime()-1;
			int step = myScenario.getClock().sampleindex(stepinitial, samplesteps);
			
			// forced sample due to knob change
			if(forcesample){
				current_sample = sample_finalTime_addNoise_applyKnob();
				return;
			}
			
			// demand is zero before stepinitial
			if(myScenario.getClock().getCurrentstep()<stepinitial)
				return;
			
			// sample the profile
			if(step<n){
				current_sample = sample_currentTime_addNoise_applyKnob();
				return;
			}
			
			// last sample
			if(step>=n && !isdone){
				current_sample = sample_finalTime_addNoise_applyKnob();
				isdone = true;
				return;
			}
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	private double sample_finalTime_addNoise_applyKnob(){	
		int step = demand_nominal.getNumTime()-1;
		return sample_KthTime_addNoise_applyKnob(step);
	}
	
	private double sample_currentTime_addNoise_applyKnob(){
		int step = myScenario.getClock().sampleindex(stepinitial, samplesteps);
		return sample_KthTime_addNoise_applyKnob(step);
	}

	/** sample the k-th entry of the profile, add noise **/
	private Double sample_KthTime_addNoise_applyKnob(int k){
				
		Double demandvalue =  demand_nominal.get(k);
		
		// add noise
		if(!isdeterministic)
			addNoise(demandvalue);

		// apply the knob 
		demandvalue = applyKnob(demandvalue);
		
		return demandvalue;
	}
	
//	private Double getKthEntry(int k){
//		return demand_nominal.get(k);
//	}
	
	
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////
	
	public BeatsTimeProfile get_demand_nominal(){
		return demand_nominal;
	}
	
	public double getCurrentValue(){
		return this.current_sample;
	}
	
	public boolean isOrphan() {
		return isOrphan;
	}

	public int getVehicle_type_index() {
		return vehicle_type_index;
	}

	public int getSamplesteps() {
		return samplesteps;
	}
	
	public double getValueForStep(int step){
		
		// hold last value
		step = Math.min(step,demand_nominal.getNumTime()-1);
		
		return demand_nominal.get(step);
	}
	
	public int getNumTime(){
		return demand_nominal.getNumTime();
	}

	protected int getStepinitial() {
		return stepinitial;
	}

	public double applyKnob(final double demandvalue){
		return demandvalue*myScenario.getGlobal_demand_knob()*_knob;
	}
	
 	public double addNoise(final double demandvalue){

 		double noisy_demand = demandvalue;
 		
		// use smallest between multiplicative and additive standard deviations
		double std_dev_apply = std_dev_mult.isInfinite() ? std_dev_add : 
															Math.min( noisy_demand*std_dev_mult , std_dev_add );
		
		// sample the distribution
		switch(myScenario.getUncertaintyModel()){
			
		case uniform:
			for(int j=0;j<myScenario.getNumVehicleTypes();j++)
				noisy_demand += BeatsMath.sampleZeroMeanUniform(std_dev_apply);
			break;

		case gaussian:
			for(int j=0;j<myScenario.getNumVehicleTypes();j++)
				noisy_demand += BeatsMath.sampleZeroMeanGaussian(std_dev_apply);
			break;
		}
		
		// non-negativity
		noisy_demand = Math.max(0.0,noisy_demand);
		
		return noisy_demand;
		
	}
}
