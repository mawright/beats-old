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

import java.math.BigDecimal;

/** Link class.
 * 
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class Link extends edu.berkeley.path.beats.jaxb.Link {
	
	// does not change ....................................
	protected Network myNetwork;
    protected Node begin_node;
    protected Node end_node;

	// link geometry
    protected double _lanes;							// [-]
    protected boolean is_queue_link;                    // true if this is a queue-type link
	
	// source/sink indicators
    protected boolean issource; 						// [boolean]
    protected boolean issink;     					    // [boolean]

    protected FundamentalDiagramProfile myFDprofile;	// fundamental diagram profile (used to rescale future FDs upon lane change event)
    protected CapacityProfile myCapacityProfile; 		// capacity profile
    protected DemandProfile myDemandProfile;  		    // demand profiles
	
	// Actuation
    protected boolean has_flow_controller;
    protected boolean has_speed_controller;
    protected double external_max_flow;
    protected double external_max_speed;
	
	// does change ........................................
	
	// link geometry
    protected double _length;							// [meters]

	// FDs
    protected FundamentalDiagram [] FDfromProfile;	    // profile fundamental diagram
    protected FundamentalDiagram FDfromEvent;			// event fundamental diagram
	
	// Events
    protected boolean activeFDevent;					// true if an FD event is active on this link,

	// link state
    protected double [] initial_density;			    // [veh]  	numVehTypes

    // link behavior
    protected LinkBehavior link_behavior;

	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	protected Link(){}

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////    

	protected void populate(Network myNetwork) {

		this.myNetwork = myNetwork;
        this.link_behavior = new LinkBehavior(this);

		// make network connections
		begin_node = myNetwork.getNodeWithId(getBegin().getNodeId());
		end_node = myNetwork.getNodeWithId(getEnd().getNodeId());

		// nodes must populate before links
		if(begin_node!=null)
			issource = begin_node.isTerminal();
		if(end_node!=null)
			issink = end_node.isTerminal();

		// lanes and length
		_lanes = getLanes();
		_length = getLength();

        has_flow_controller = false;
        has_speed_controller = false;

        is_queue_link = getLinkType()==null ? false : getLinkType().getName().compareToIgnoreCase("Intersection Approach")==0;
				
	}

	protected void validate() {

		if(!issource && begin_node==null)
			BeatsErrorLog.addError("Incorrect begin node ID=" + getBegin().getNodeId() + " in link ID=" + getId() + ".");

		if(!issink && end_node==null)
			BeatsErrorLog.addError("Incorrect node ID=" + getEnd().getNodeId() + " in link ID=" + getId() + ".");

		if(_length<=0)
			BeatsErrorLog.addError("Non-positive length in link ID=" + getId() + ".");

		if(_lanes<=0)
			BeatsErrorLog.addError("Non-positive number of lanes in link ID=" + getId() + ".");
	}

	protected void reset(){
		resetLanes();		
		resetState();
		resetFD();
		
		this.external_max_flow = Double.POSITIVE_INFINITY;
		this.external_max_speed = Double.POSITIVE_INFINITY;
	}
	
	private void resetState() {

        link_behavior.reset(initial_density);


		return;
	}

	private void resetLanes(){
		_lanes = getLanes();
	}

	private void resetFD(){
		FDfromProfile = new FundamentalDiagram [myNetwork.getMyScenario().getNumEnsemble()];
		for(int i=0;i<FDfromProfile.length;i++){
			FDfromProfile[i] = new FundamentalDiagram(this);
			FDfromProfile[i].settoDefault();
		}
		FDfromEvent = null;
		activeFDevent = false;
	}

	protected void update() {
        link_behavior.update();
	}

    protected void updateOutflowDemand(){
        link_behavior.updateOutflowDemand(external_max_speed,external_max_flow);
    }

    protected void updateSpaceSupply(){
        link_behavior.updateSpaceSupply();
    }

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////

	// demand profiles .................................................
	protected void setMyDemandProfile(DemandProfile x){
		this.myDemandProfile = x;
	}

	// capcity profile .................................................
	protected void setMyCapacityProfile(CapacityProfile x){
		this.myCapacityProfile = x;
	}
	
	// Events ..........................................................

	// used by Event.setLinkFundamentalDiagram to activate an FD event
	protected void activateFDEvent(edu.berkeley.path.beats.jaxb.FundamentalDiagram fd) throws BeatsException {
		if(fd==null)
			return;

		FDfromEvent = new FundamentalDiagram(this,currentFD(0));		// copy current FD 
		// note: we are copying from the zeroth FD for simplicity. The alternative is to 
		// carry numEnsemble event FDs.
		FDfromEvent.copyfrom(fd);			// replace values with those defined in the event

		BeatsErrorLog.clearErrorMessage();
		FDfromEvent.validate();
		if(BeatsErrorLog.haserror())
			throw new BeatsException("Fundamental diagram event could not be validated.");

		activeFDevent = true;
	}

	// used by Event.revertLinkFundamentalDiagram
	protected void revertFundamentalDiagramEvent() throws BeatsException{
		if(!activeFDevent)
			return;
		activeFDevent = false;
	}

	// used by Event.setLinkLanes
	protected void set_Lanes(double newlanes) throws BeatsException{
		for(int e=0;e<myNetwork.getMyScenario().getNumEnsemble();e++)
			if(getDensityJamInVeh(e)*newlanes/get_Lanes() < link_behavior.getTotalDensityInVeh(e))
				throw new BeatsException("ERROR: Lanes could not be set.");

		if(myFDprofile!=null)
			myFDprofile.set_Lanes(newlanes);	// adjust present and future fd's
		for(int e=0;e<myNetwork.getMyScenario().getNumEnsemble();e++)
			FDfromProfile[e].setLanes(newlanes);
		_lanes = newlanes;					// adjust local copy of lane count
	}

	// FD profile ......................................................

	// called by FundamentalDiagramProfile.populate,
	protected void setFundamentalDiagramProfile(FundamentalDiagramProfile fdp){
		if(fdp==null)
			return;
		myFDprofile = fdp;
	}

	// used by FundamentalDiagramProfile to set the FD
	protected void setFDFromProfile(FundamentalDiagram fd) throws BeatsException{
		if(fd==null)
			return;

		// sample the fundamental digram
		for(int e=0;e<myNetwork.getMyScenario().getNumEnsemble();e++)
			FDfromProfile[e] = fd.perturb();
	}

	// controller registration .........................................

    public boolean register_flow_controller(){
        if(has_flow_controller)
            return false;
        has_flow_controller = true;
        return true;
    }

    public boolean register_speed_controller(){
        if(has_speed_controller)
            return false;
        has_speed_controller = true;
        return true;
    }

    public void set_external_max_flow_in_veh(double value_in_veh){
        if(has_flow_controller)
            external_max_flow = value_in_veh;
    }

	public void set_external_max_flow_in_vph(double value_in_vph){
        if(has_flow_controller)
            external_max_flow = value_in_vph*getMyNetwork().getMyScenario().getSimdtinseconds()/3600d;
	}
	
    public void set_external_max_speed(double value){
        if(has_speed_controller)
            external_max_speed = value;
    }

	// initial condition ..................................................
	protected void copy_state_to_initial_state(){
		initial_density = link_behavior.density[0].clone();
	}
	
	protected void set_initial_state(double [] d){
		initial_density  = d==null ? BeatsMath.zeros(myNetwork.getMyScenario().getNumVehicleTypes()) : d.clone();
	}

	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	public static boolean haveSameType(Link linkA,Link linkB){
		String typeA = linkA.getType();
		String typeB = linkB.getType();
		if(typeA==null || typeB==null)
			return false;
		return typeA.equalsIgnoreCase(typeB);
	}
	
	public String getType(){
		if(getLinkType()==null)
			return null;
		if(getLinkType().getName()==null)
			return null;
		return getLinkType().getName();
	}
	
	public boolean isOnramp(){
		String name = getType();
		return name==null ? null : name.compareToIgnoreCase("On-Ramp")==0;
	}

    public boolean isOfframp(){
        String name = getType();
        return name==null ? null : name.compareToIgnoreCase("Off-Ramp")==0;
    }

	public boolean isFreeway(){
		String name = getType();
		return name==null ? null : name.compareToIgnoreCase("Freeway")==0;
	}

	// Link geometry ....................

	/** network that contains this link */
	public Network getMyNetwork() {
		return myNetwork;
	}

	/** upstream node of this link  */
	public Node getBegin_node() {
		return begin_node;
	}

	/** downstream node of this link */
	public Node getEnd_node() {
		return end_node;
	}

	/** Length of this link in meters */
	public double getLengthInMeters() {
		return _length;
	}

	/** Number of lanes in this link */
	public double get_Lanes() {
		return _lanes;
	}

	/** <code>true</code> if this link is a source of demand into the network */
	public boolean isSource() {
		return issource;
	}

	/** <code>true</code> if this link is a sink of demand from the network */
	public boolean isSink() {
		return issink;
	}

	// Link weaving and merging behavior
	
	public double getPriority(int ensemble) {
		BigDecimal priority = getPriority();
		return null != priority ? priority.doubleValue() : getCapacityInVeh(ensemble);
	}
	
	// Link state .......................

    public double[] getDensityInVeh(int ensemble) {
        return link_behavior.getDensityInVeh(ensemble);
    }

    public double getDensityInVeh(int ensemble,int vehicletype) {
        return link_behavior.getDensityInVeh(ensemble,vehicletype);
    }

    public double getTotalDensityInVeh(int ensemble) {
        return link_behavior.getTotalDensityInVeh(ensemble);
    }

    public boolean set_density(double [] d){
        return link_behavior.set_density(d);
    }

    public double[] get_out_demand_in_veh(int ensemble) {
        return link_behavior.get_out_demand_in_veh(ensemble);
    }

    public double get_space_supply_in_veh(int ensemble) {
        return link_behavior.get_space_supply_in_veh(ensemble);
    }


    protected void setInflow(int ensemble,double[] inflow) {
        link_behavior.setInflow(ensemble,inflow);
    }

    protected void setOutflow(int ensemble,double[] outflow) {
        link_behavior.setOutflow(ensemble,outflow);
    }

    public double[] getOutflowInVeh(int ensemble) {
        return link_behavior.getOutflowInVeh(ensemble);
    }

    public double getTotalOutflowInVeh(int ensemble) {
        return link_behavior.getTotalOutflowInVeh(ensemble);
    }

    public double getInflowInVeh(int ensemble,int vehicletype) {
        return link_behavior.getInflowInVeh(ensemble,vehicletype);
    }

    public double[] getInflowInVeh(int ensemble) {
        return link_behavior.getInflowInVeh(ensemble);
    }

    public double getTotalInlowInVeh(int ensemble) {
        return link_behavior.getTotalInlowInVeh(ensemble);
    }

    public double getOutflowInVeh(int ensemble,int vehicletype) {
        return link_behavior.getOutflowInVeh(ensemble,vehicletype);
    }

    public double getTotalDensityInVPMeter(int ensemble) {
        return link_behavior.getTotalDensityInVPMeter(ensemble);
    }

    public double computeTotalDelayInVeh(int ensemble){
        return link_behavior.computeTotalDelayInVeh(ensemble);
    }

    public double computeDelayInVeh(int ensemble,int vt_index){
        return link_behavior.computeDelayInVeh(ensemble,vt_index);
    }

    public double computeSpeedInMPS(int ensemble){
        return link_behavior.computeSpeedInMPS(ensemble);
    }

    public void overrideDensityWithVeh(double[] x,int ensemble){
        link_behavior.overrideDensityWithVeh(x,ensemble);
    }



        // Fundamental diagram ....................

	/** Jam density in vehicle/link. */
	public double getDensityJamInVeh(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getDensityJamInVeh();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Critical density in vehicle/link. */
	public double getDensityCriticalInVeh(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getDensityCriticalInVeh();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Capacity drop in vehicle/simulation time step */
	public double getCapacityDropInVeh(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getCapacityDropInVeh();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Capacity in vehicle/simulation time step */
	public double getCapacityInVeh(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getCapacityInVeh();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Jam density in vehicle/meter/lane. */
	public double getDensityJamInVPMPL(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getDensityJamInVeh() / getLengthInMeters() / _lanes;
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Critical density in vehicle/meter/lane. */
	public double getDensityCriticalInVPMPL(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getDensityCriticalInVeh() / getLengthInMeters() / _lanes;
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Capacity drop in vehicle/second/lane. */
	public double getCapacityDropInVPSPL(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getCapacityDropInVeh() / myNetwork.getMyScenario().getSimdtinseconds() / _lanes;
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Capacity in vehicles per second. */
	public double getCapacityInVPS(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getCapacityInVeh() / myNetwork.getMyScenario().getSimdtinseconds();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Capacity in vehicle/second/lane. */
	public double getCapacityInVPSPL(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD._getCapacityInVeh() / myNetwork.getMyScenario().getSimdtinseconds() / _lanes;
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Freeflow speed in normalized units (link/time step). */
	public double getNormalizedVf(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getVfNormalized();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Freeflow speed in meters/second. */
	public double getVfInMPS(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getVfNormalized() * getLengthInMeters() / myNetwork.getMyScenario().getSimdtinseconds();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Critical speed in meters/second. */
	public double getCriticalSpeedInMPS(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if (null == FD)
				return Double.NaN;
			else if (null != FD.getCriticalSpeed())
				return FD.getCriticalSpeed().doubleValue();
			else
				return getVfInMPS(ensemble);
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Congestion wave speed in normalized units (link/time step). */
	public double getNormalizedW(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getWNormalized();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** Congestion wave speed in meters/second. */
	public double getWInMPS(int ensemble) {
		try {
			FundamentalDiagram FD = currentFD(ensemble);
			if(FD==null)
				return Double.NaN;
			return FD.getWNormalized() * getLengthInMeters() / myNetwork.getMyScenario().getSimdtinseconds();
		} catch (Exception e) {
			return Double.NaN;
		}
	}



    public DemandProfile getDemandProfile(){
        return myDemandProfile;
    }

    public FundamentalDiagramProfile getFundamentalDiagramProfile(){
        return myFDprofile;
    }


    /////////////////////////////////////////////////////////////////////
	// private
	/////////////////////////////////////////////////////////////////////

	// getter for the currently active fundamental diagram
	protected FundamentalDiagram currentFD(int ensemble){
		if(activeFDevent)
			return FDfromEvent;
		return FDfromProfile==null ? null : FDfromProfile[ensemble];
	}


}
