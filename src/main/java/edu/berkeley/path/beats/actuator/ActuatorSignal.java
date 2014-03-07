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

package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.Phase;
import edu.berkeley.path.beats.simulator.*;

import java.util.*;

/** ActuatorSignal class.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class ActuatorSignal extends Actuator {

    public static enum CommandType {hold,forceoff};
    public static enum BulbColor {GREEN,YELLOW,RED,DARK};
//    public static enum NEMA {NULL,_1,_2,_3,_4,_5,_6,_7,_8};

	private HashMap<NEMA.ID,SignalPhase> nema2phase;
	private Node myNode;
	private ArrayList<SignalPhase> phases;

	// local copy of the command, subject to checks
	private boolean [] hold_approved;
	private boolean [] forceoff_approved;
	
//	private ArrayList<PhaseData> completedPhases = new ArrayList<PhaseData>(); // used for output

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public ActuatorSignal(edu.berkeley.path.beats.jaxb.Signal jaxbS,ActuatorImplementation act_implementor){

        super(act_implementor,Type.signal);

        // set ID for this acuator
        setId(jaxbS.getId());
    }

    /////////////////////////////////////////////////////////////////////
    // actuation command
    /////////////////////////////////////////////////////////////////////

    public void set_command(ArrayList<ActuatorSignal.Command> command){
        for(ActuatorSignal.Command c : command){
            SignalPhase p = nema2phase.get(c.nema);
            if(p==null)
                continue;

            switch(c.type){
                case forceoff:
                    p.setForceoff_requested(true);
                    p.setActualyellowtime( c.yellowtime>=0 ? c.yellowtime : p.getYellowtime() );
                    p.setActualredcleartime( c.redcleartime>=0 ? c.redcleartime : p.getRedcleartime() );
                    break;

                case hold:
                    p.setHold_requested(true);
                    break;
            }
        }
    }

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / deploy
	/////////////////////////////////////////////////////////////////////

    @Override
	protected void populate(Object jaxbobject,Scenario myScenario) {

        edu.berkeley.path.beats.jaxb.Signal jaxbSignal = (edu.berkeley.path.beats.jaxb.Signal)jaxbobject;

		myNode = myScenario.getNodeWithId(jaxbSignal.getNodeId());
		
		if(myNode==null)
			return;

        // make list of phases and map
		phases = new ArrayList<SignalPhase>();
		nema2phase = new HashMap<NEMA.ID,SignalPhase>();
        HashMap<NEMA.ID,List<Link>> nema_to_linklist = (HashMap<NEMA.ID,List<Link>>) implementor.get_target();
        for(Phase jphase : jaxbSignal.getPhase() ){
            NEMA.ID nema = NEMA.int_to_nema(jphase.getNema().intValue());
            List<Link> link_list = nema_to_linklist.get( nema );
            if(link_list!=null){
                SignalPhase sp = new SignalPhase(myNode,this,myScenario.getSimdtinseconds());
                sp.populateFromJaxb(myScenario,jphase);
                phases.add(sp);
                nema2phase.put(nema,sp);
            }
        }

        // command vector
		hold_approved = new boolean[phases.size()];
		forceoff_approved = new boolean[phases.size()];
	}

    @Override
	protected void reset() {
		if(myNode==null)
			return;
		for(SignalPhase p : phases)
			p.reset();
	}

    @Override
	protected void validate() {
		
		if(myNode==null){
			BeatsErrorLog.addWarning("Unknow node ID in signal ID=" + getId());
			return; // this signal will be ignored
		}
		
		if(phases==null)
			BeatsErrorLog.addError("ActuatorSignal ID=" + getId() + " contains no valid phases.");
        else
			for(SignalPhase p : phases)
				p.validate();
	}

    @Override
	protected void deploy(double current_time_in_seconds) {

		if(myNode==null)
			return;

		// 0) Advance all phase timers ...........................................
		for(SignalPhase phase : phases)
            phase.getBulbtimer().advance();

		// Update detector stations ............................................
//		for(SignalPhase phase : phases)
//			phase.UpdateDetectorStations();

//		// Update stopline calls
//		for(SignalPhase phase : phases)
//		    phase.hasstoplinecall = phase.Recall() ? true : phase.StoplineStation()!=null && phase.StoplineStation().GotCall();
//
//		// Update approach calls
//		for(SignalPhase phase : phases)
//			phase.hasapproachcall = phase.ApproachStation()!=null && phase.ApproachStation().GotCall();
//
//		// Update conflicting calls
//        for(SignalPhase phase : phases)
//            phase.currentconflictcall = CheckForConflictingCall(i);
//        for(SignalPhase phase : phases){
//			if( !phase.hasconflictingcall && phase.currentconflictcall )
//                phase.conflictingcalltime = (float)(myNode.getMyNetwork().getSimTime()*3600f);
//            phase.hasconflictingcall = phase.currentconflictcall;
//        }

		for(SignalPhase phase:phases)
			phase.updatePermitOpposingHold();
		
		// 3) Update permitted holds ............................................
		for(SignalPhase phaseA:phases){
            phaseA.setPermithold(true);
			for(SignalPhase phaseB:phases)
				if(!NEMA.isCompatible(phaseA, phaseB) && !phaseB.isPermitopposinghold() )
                    phaseA.setPermithold(false);
		}
		
		// 4) Update signal commands ...................................................
		
		// Throw away conflicting hold pairs 
		// (This is purposely drastic to create an error)
		for(SignalPhase phaseA:phases)
			if(phaseA.isHold_requested())
				for(SignalPhase phaseB:phases)
					if( phaseB.isHold_requested() && !NEMA.isCompatible(phaseA, phaseB) ){
                        phaseA.setHold_requested(false);
                        phaseB.setHold_requested(false);
					}

		// Deal with simultaneous hold and forceoff (RHODES needs this)
		for(SignalPhase phase: phases)
			if( phase.isHold_requested() && phase.isForceoff_requested() )
                phase.setForceoff_requested(false);

		// Make local relaying copy
        for(int i=0;i<phases.size();i++){
            SignalPhase phase = phases.get(i);
            hold_approved[i] = phase.isHold_requested();
            forceoff_approved[i] = phase.isForceoff_requested();
		}

		// No transition if no permission
        for(int i=0;i<phases.size();i++)
			if( !phases.get(i).isPermithold() )
				hold_approved[i] = false;

		// No transition if green time < mingreen
        for(int i=0;i<phases.size();i++)
            if( phases.get(i).getBulbColor().compareTo(BulbColor.GREEN)==0  && BeatsMath.lessthan(phases.get(i).getBulbtimer().getT(), phases.get(i).getMingreen()) )
				forceoff_approved[i] = false;

		// collect updated bulb indications
        ActuatorSignal.BulbColor [] new_bulb_colors = new ActuatorSignal.BulbColor[phases.size()];
		for(int i=0;i<phases.size();i++){
            SignalPhase phase = phases.get(i);
            new_bulb_colors[i] = phase.get_new_bulb_color(hold_approved[i],forceoff_approved[i]);

            // set phase color if changed
            if(new_bulb_colors[i]!=null)
                phases.get(i).bulbcolor = new_bulb_colors[i];
        }

//        // Remove serviced commands
//		for(SignalPhase phase: phases){
//			if(phase.isGreen())
//                phase.setHold_requested(false);
//			if(phase.isYellow() || phase.isRed() )
//                phase.setForceoff_requested(false);
//		}
//
//		// Set permissive opposing left turn to yellow
//		// opposing is yellow if I am green or yellow, and I am through, and opposing is permissive
//		// opposing is red if I am red and it is not protected
//		for(SignalPhase phase : phases){
//			SignalPhase ophase = phase.getOpposingPhase();
//			if(ophase==null)
//				continue;
//			switch(phase.getBulbColor()){
//				case GREEN:
//				case YELLOW:
//					if(phase.isIsthrough() && ophase.isPermissive())
//                        ophase.setPhaseColor(ActuatorSignal.BulbColor.YELLOW);
//					break;
//				case RED:
//					if(!ophase.isProtected())
//						ophase.setPhaseColor(ActuatorSignal.BulbColor.RED);
//					break;
//			case DARK:
//				break;
//			default:
//				break;
//			}
//		}

        // deploy to dynamics
//        implementor().deploy_bulb_color(myNEMA, new_bulb_colors);

	}

    @Override
    protected boolean register() {
        HashMap<NEMA.ID,List<Link>> phase_link_map = (HashMap<NEMA.ID,List<Link>>) implementor.get_target();
        boolean success = true;
        for(List<Link> link_list : phase_link_map.values())
            for(Link link : link_list)
                success &= link.register_flow_controller();
        return success;
    }

    /////////////////////////////////////////////////////////////////////
	// protected
	/////////////////////////////////////////////////////////////////////
	
//	protected boolean register(){
//		if(myNode==null)
//			return true;
//		else
//			return myPhaseController.register();
//	}

	protected SignalPhase getPhaseForNEMA(NEMA.ID nema){
		for(SignalPhase p:phases){
			if(p!=null)
				if(p.getNEMA().compareTo(nema)==0)
					return p;
		}
		return null;
	}
	
//	protected SignalPhaseController getMyPhaseController() {
//		return myPhaseController;
//	}

//	protected java.util.List<PhaseData> getCompletedPhases() {
//		return completedPhases;
//	}
	
	/////////////////////////////////////////////////////////////////////
	// public methods
	/////////////////////////////////////////////////////////////////////

	public SignalPhase getPhaseByNEMA(NEMA.ID nema){
		if(nema==null)
			return null;
		return nema2phase.get(nema);
	}

    public Long get_node_id(){
        return myNode==null ? null : myNode.getId();
    }

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	public static class Command implements Comparable {
		public ActuatorSignal.CommandType type;
		public NEMA.ID nema;
		public Double time;
		public Double yellowtime;
		public Double redcleartime;

		public Command(ActuatorSignal.CommandType type,NEMA.ID phase,double time){
			this.type = type;
			this.nema = phase;
			this.time = time;
			this.yellowtime = Double.NaN;
			this.redcleartime = Double.NaN;
		}
		
		public Command(ActuatorSignal.CommandType type,NEMA.ID phase,double time,double yellowtime,double redcleartime){
			this.type = type;
			this.nema = phase;
			this.time = time;
			this.yellowtime = yellowtime;
			this.redcleartime = redcleartime;
		}
		
		@Override
		public int compareTo(Object arg0) {
			
			if(arg0==null)
				return 1;
			
			int compare;
			Command that = (Command) arg0;
			
			// first ordering by time stamp
			Double thiststamp = this.time;
			Double thattstamp = that.time;
			compare = thiststamp.compareTo(thattstamp);
			if(compare!=0)
				return compare;

			// second ordering by phases
			NEMA.ID thistphase = this.nema;
			NEMA.ID thattphase = that.nema;
			compare = thistphase.compareTo(thattphase);
			if(compare!=0)
				return compare;
			
			// third ordering by type
			CommandType thisttype = this.type;
			CommandType thatttype = that.type;
			compare = thisttype.compareTo(thatttype);
			if(compare!=0)
				return compare;

			// fourth ordering by yellowtime
			Double thistyellowtime = this.yellowtime;
			Double thattyellowtime = that.yellowtime;
			compare = thistyellowtime.compareTo(thattyellowtime);
			if(compare!=0)
				return compare;

			// fifth ordering by redcleartime
			Double thistredcleartime = this.redcleartime;
			Double thattredcleartime = that.redcleartime;
			compare = thistredcleartime.compareTo(thattredcleartime);
			if(compare!=0)
				return compare;
			
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj==null)
				return false;
			else
				return this.compareTo((Command) obj)==0;
		}

        @Override
        public String toString() {
            return time + ": " + type.toString() + " " + nema + " (y=" + yellowtime + ",r=" + redcleartime + ")";
        }
    }

	public class PhaseData{
		public NEMA.ID nema;
		public double starttime;
		public double greentime;
		public PhaseData(NEMA.ID nema, double starttime, double greentime){
			this.nema = nema;
			this.starttime = starttime;
			this.greentime = greentime;
		}
	}

}



