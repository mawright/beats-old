package com.relteq.sirius.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.relteq.sirius.simulator.ObjectFactory;
import com.relteq.sirius.simulator.SiriusErrorLog;
import com.relteq.sirius.simulator.SiriusException;
import com.relteq.sirius.simulator.Event;
import com.relteq.sirius.simulator.Link;
import com.relteq.sirius.simulator.Scenario;
import com.relteq.sirius.simulator.ScenarioElement;

public class Event_Fundamental_Diagram extends Event {

	protected boolean resetToNominal;
	protected com.relteq.sirius.jaxb.FundamentalDiagram FD;
	  
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public Event_Fundamental_Diagram(){
	}
	
	public Event_Fundamental_Diagram(Scenario myScenario,List <Link> links,double freeflowSpeed,double congestionSpeed,double capacity,double densityJam,double capacityDrop,double stdDevCapacity) {		
		this.FD = new com.relteq.sirius.jaxb.FundamentalDiagram();
		this.FD.setFreeFlowSpeed(new BigDecimal(freeflowSpeed));
		this.FD.setCongestionSpeed(new BigDecimal(congestionSpeed));
		this.FD.setCapacity(new BigDecimal(capacity));
		this.FD.setJamDensity(new BigDecimal(densityJam));
		this.FD.setCapacityDrop(new BigDecimal(capacityDrop));
		this.FD.setStdDevCapacity(new BigDecimal(stdDevCapacity));
		this.resetToNominal = false;
		this.targets = new ArrayList<ScenarioElement>();
		for(Link link : links)
			this.targets.add(ObjectFactory.createScenarioElement(link));
	}
	
	public Event_Fundamental_Diagram(Scenario myScenario,List <Link> links) {		
		this.resetToNominal = true;
		for(Link link : links)
			this.targets.add(ObjectFactory.createScenarioElement(link));
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceEvent
	/////////////////////////////////////////////////////////////////////

	@Override
	public void populate(Object jaxbobject) {
		com.relteq.sirius.jaxb.Event jaxbe = (com.relteq.sirius.jaxb.Event) jaxbobject;
		this.resetToNominal = jaxbe.isResetToNominal();
		this.FD = jaxbe.getFundamentalDiagram();
	}
	
	@Override
	public boolean validate() {
		if(!super.validate())
			return false;
		
		// check each target is valid
		for(ScenarioElement s : targets){
			if(s.getMyType().compareTo(ScenarioElement.Type.link)!=0){
				SiriusErrorLog.addErrorMessage("wrong target type.");
				return false;
			}
		}
		
		// check that new fundamental diagram does not invalidate current state
		
		return true;
	}

	@Override
	public void activate() throws SiriusException{
		for(ScenarioElement s : targets){
			Link targetlink = (Link) s.getReference();
			if(resetToNominal)
				revertLinkFundamentalDiagram(targetlink);
			else
				setLinkFundamentalDiagram(targetlink,FD);
		}
		
	}
}
