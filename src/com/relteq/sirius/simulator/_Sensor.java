/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

public abstract class _Sensor implements InterfaceSensor {

	protected String id;
	protected Types.Sensor myType;
	protected _Link myLink = null;

	// copy jaxb info in constructor instead of initialize
	// because _Sensor does not inherit Sensor, so the data is not
	// available to this object. _Sensor is not a subclass of Sensor
	// because what we are really calling are the specific implementations such
	// as SensorLoopStation, and these cannot be generated by createSensor.
	public _Sensor(com.relteq.sirius.jaxb.Sensor s,Types.Sensor myType){
		this.myType = myType;
		this.id = s.getId();
		if(s.getLinkReference()!=null)
			myLink = Utils.getLinkWithCompositeId(s.getLinkReference().getNetworkId(),s.getLinkReference().getId());
	}


	/////////////////////////////////////////////////////////////////////
	// interface
	/////////////////////////////////////////////////////////////////////

	public String getId() {
		return id;
	}

	public Types.Sensor getMyType() {
		return myType;
	}

	public _Link getMyLink() {
		return myLink;
	}
	
	
}
