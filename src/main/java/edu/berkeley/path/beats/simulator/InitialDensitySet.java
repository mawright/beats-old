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

import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.simulator.utils.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.utils.BeatsMath;

/** XXX.
 * YYY
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public final class InitialDensitySet extends edu.berkeley.path.beats.jaxb.InitialDensitySet {

	// does not change ....................................
	private Scenario myScenario;
	private Link [] link;					// ordered array of references
	private int [] vehicle_type_index; 		// index of vehicle types into global list
	private Double [] initial_density; 		// [veh/meter] indexed by link and type

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate(Scenario myScenario){
				
		this.myScenario = myScenario;

		// allocate
		int numLinks = getDensity().size();
		initial_density = BeatsMath.zeros(numLinks);
		link = new Link [numLinks];
		vehicle_type_index = new int [numLinks];
		
		// copy profile information to arrays in extended object
		for(int i=0;i<numLinks;i++){
			edu.berkeley.path.beats.jaxb.Density density = getDensity().get(i);
			link[i] = myScenario.get.linkWithId(density.getLinkId());
			vehicle_type_index[i] = myScenario.get.vehicleTypeIndexForId(density.getVehicleTypeId());
			if(link[i]!=null && vehicle_type_index[i]>=0){
				initial_density[i] = Double.parseDouble(density.getContent()); 
			}
		}
		
	}

	protected void validate() {
		
		int i;
		
//		// check that all vehicle types are accounted for
//		if(vehicletypeindex.length!=myScenario.getNumVehicleTypes())
//			BeatsErrorLog.addError("List of vehicle types in initial density profile ID=" + getId() + " does not match that of settings.");
//		
//		// check that vehicle types are valid
//		for(i=0;i<vehicletypeindex.length;i++)
//			if(vehicletypeindex[i]<0)
//				BeatsErrorLog.addError("Bad vehicle type name in initial density profile ID=" + getId());
//		
//		// check size of data
//		if(link!=null)
//			for(i=0;i<link.length;i++)
//				if(initial_density[i].length!=vehicletypeindex.length)
//					BeatsErrorLog.addError("Number of density values does not match number of vehicle types in initial density profile ID=" + getId());

		// check that values are between 0 and jam density
		int j;
		double sum;
		double x;
		for(i=0;i<initial_density.length;i++){
			
			if(link[i]==null){
				BeatsErrorLog.addWarning("Unknown link ID in initial density profile");
				continue;
			}
			
			if(link[i].isSource())	// does not apply to source links
				continue;
			
//			sum = 0.0;
//			for(j=0;j<vehicletypeindex.length;j++){
//				x = initial_density[i][j];
//				if(x<0)
//					BeatsErrorLog.addError("Negative value found in initial density profile for link ID=" + link[i].getId());
//				if( x.isNaN())
//					BeatsErrorLog.addError("Invalid value found in initial density profile for link ID=" + link[i].getId());
//				sum += x;
//			}
			
			// NOTE: REMOVED THIS CHECK TEMPORARILY. NEED TO DECIDE HOW TO DO IT 
			// WITH ENSEMBLE FUNDAMENTAL DIAGRAMS
//			if(sum>link[i].getDensityJamInVPMPL())
//				BeatsErrorLog.addErrorMessage("Initial density exceeds jam density.");

		}		
	}

	protected void reset() {
	}

	protected void update() {
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	/** Get the initial density for a link identified by network and link ID.
	 *
	 * @param network_id String ID of the network
	 * @param linkid String ID of the link
	 * @return array of intitial densities in [veh/link]
	 */
	public Double [] getDensityForLinkIdInVeh(long network_id,long linkid){
		Double [] d = BeatsMath.zeros(myScenario.get.numVehicleTypes());
		boolean foundit = false;
		for(int i=0;i<link.length;i++){
			if(link[i]!=null && link[i].getId()==linkid && link[i].myNetwork.getId()==network_id){
				foundit = true;
				d[vehicle_type_index[i]] = initial_density[i] * link[i].getLengthInMeters();
			}
		}
		if(foundit)
			return d;
		return null;
	}

//	/** Get the initial densities in [veh/meter]
//	 * 
//	 * @return 2D array of doubles indexed by link and vehicle type
//	 */
//	public double[] get_initial_density_in_vehpermeter() {
//		return initial_density;
//	}

//	/** Get the initial densities in [veh]
//	 * 
//	 * @return 2D array of doubles indexed by link and vehicle type
//	 */
//	public double[] get_initial_density_in_veh() {
//		Double [] X = new Double [link.length][myScenario.getNumVehicleTypes()];
//		int i,j;
//		double linklength;
//		for(i=0;i<link.length;i++){
//			linklength = link[i].getLengthInMeters();
//			for(j=0;j<myScenario.getNumVehicleTypes();j++)
//				X[i][j] = initial_density[i][j] * linklength;
//		}
//		return X;
//	}
//
//	/** Array of links included in the initial density set. 
//	 * 
//	 * @return array of Links
//	 */
//	public Link[] getLink() {
//		return link;
//	}


    @Override
    public String toString() {
        String str = "";
        for(Density d:getDensity())
            str += String.format("%d\t%d\t%s\n",d.getLinkId(),d.getVehicleTypeId(),d.getContent());
        return str;
    }
}
