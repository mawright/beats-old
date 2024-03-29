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

package edu.berkeley.path.beats.event;

import edu.berkeley.path.beats.simulator.utils.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.utils.BeatsException;
import edu.berkeley.path.beats.simulator.Event;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;

public class Event_Link_Lanes extends Event {

	protected boolean resetToNominal;
	protected double deltalanes;
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Event_Link_Lanes(){
	}
	
	public Event_Link_Lanes(Scenario myScenario,edu.berkeley.path.beats.jaxb.Event jaxbE,Event.Type myType){
		super(myScenario, jaxbE, myType);
	}
			
//	public Event_Link_Lanes(Scenario myScenario,List<Link> links,boolean isrevert,double deltalanes) {
//		this.targets = new ArrayList<ScenarioElement>();
//		this.resetToNominal = isrevert;
//		for(Link link : links)
//			this.targets.add(ObjectFactory.createScenarioElement(link));
//		this.deltalanes = deltalanes;
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / activate
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		edu.berkeley.path.beats.jaxb.Event jaxbe = (edu.berkeley.path.beats.jaxb.Event) jaxbobject;
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) jaxbe.getParameters();
		// reset_to_nominal
		if (null != params && params.has("reset_to_nominal"))
			this.resetToNominal = params.get("reset_to_nominal").equalsIgnoreCase("true");
		else
			this.resetToNominal = false;
		// lane_count_change
		if (null != params && params.has("lane_count_change"))
			this.deltalanes = Double.valueOf(params.get("lane_count_change"));
		else
			this.deltalanes = 0.0;
	}

	@Override
	protected void validate() {
		
		super.validate();
		
		// check each target is valid
		for(ScenarioElement s : getTargets()){
			if(s.getMyType()!=ScenarioElement.Type.link)
				BeatsErrorLog.addError("wrong target type for event ID=" +getId() +".");
		}
	}
	
	@Override
	protected void activate() throws BeatsException{
		double newlanes;
		for(ScenarioElement s : getTargets()){
			Link targetlink = (Link) s.getReference();
			if(resetToNominal)
				newlanes = ((edu.berkeley.path.beats.jaxb.Link)targetlink).getLanes();
			else
				newlanes =  targetlink.get_Lanes();
			newlanes += deltalanes;
            targetlink.set_Lanes(newlanes);
		}
	}

}
