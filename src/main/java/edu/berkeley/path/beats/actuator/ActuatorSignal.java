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
    public static enum NEMA {NULL,_1,_2,_3,_4,_5,_6,_7,_8};

	private HashMap<NEMA,SignalPhase> nema2phase;
	private Node myNode;
<<<<<<< HEAD
	private ArrayList<SignalPhase> phases;
=======
//	private SignalPhaseController myPhaseController;	// used to control capacity on individual links
	private SignalPhase [] phases;
>>>>>>> calpath/arterial
	
	// local copy of the command, subject to checks
	private boolean [] hold_approved;
	private boolean [] forceoff_approved;
	
//	private ArrayList<PhaseData> completedPhases = new ArrayList<PhaseData>(); // used for output

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public ActuatorSignal(edu.berkeley.path.beats.jaxb.Signal jaxbS,ActuatorImplementation act_implementor){

        super(act_implementor,Type.signal);

        // set id for this acuator
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
<<<<<<< HEAD
=======
		
		int i;
		int totalphases = jaxbSignal.getPhase().size();
		
		if(totalphases==0)
			return;
		
		// ignore phases without targets, or !permissive && !protected
		// (instead of throwing validation error, because network editor 
		// generates phases like this).
		boolean [] isvalid = new boolean[totalphases];
		int numlinks;
		int numvalid = 0;
		for(i=0;i<jaxbSignal.getPhase().size();i++){
			edu.berkeley.path.beats.jaxb.Phase p = jaxbSignal.getPhase().get(i);
			isvalid[i] = true;
			isvalid[i] &= p.isPermissive() || p.isProtected();
			if (null == p.getLinkReferences())
				numlinks = 0;
			else
				numlinks = p.getLinkReferences().getLinkReference().size();
			isvalid[i] &= numlinks>0;
			numvalid += isvalid[i] ? 1 : 0;
		}
		
		phases = new SignalPhase[numvalid];
		nema2phase = new HashMap<NEMA,SignalPhase>(numvalid);
		int c = 0;
		for(i=0;i<jaxbSignal.getPhase().size();i++){
			if(!isvalid[i])
				continue;
			phases[c] = new SignalPhase(myNode,this,myScenario.getSimdtinseconds());
			phases[c].populateFromJaxb(myScenario,jaxbSignal.getPhase().get(i));
			nema2phase.put(phases[c].getNEMA(),phases[c]);
			c++;
		}
		
		hold_approved = new boolean[phases.length];
		forceoff_approved = new boolean[phases.length];
		
		// create myPhaseController. This is used to implement flow control on target links
//		myPhaseController = new SignalPhaseController(this);
		
		// nema2phase
>>>>>>> calpath/arterial

        // make list of phases and map
		phases = new ArrayList<SignalPhase>();
		nema2phase = new HashMap<NEMA,SignalPhase>();
        HashMap<ActuatorSignal.NEMA,List<Link>> nema_to_linklist = (HashMap<ActuatorSignal.NEMA,List<Link>>) implementor.get_target();
        for(Phase jphase : jaxbSignal.getPhase() ){
            ActuatorSignal.NEMA nema = SignalPhase.int_to_nema(jphase.getNema().intValue());
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
<<<<<<< HEAD
		for(SignalPhase p : phases)
=======
		for(SignalPhase p : phases){
>>>>>>> calpath/arterial
			p.reset();
	}

    @Override
	protected void validate() {
		
		if(myNode==null){
			BeatsErrorLog.addWarning("Unknow node id in signal id=" + getId());
			return; // this signal will be ignored
		}
		
		if(phases==null)
			BeatsErrorLog.addError("ActuatorSignal id=" + getId() + " contains no valid phases.");

<<<<<<< HEAD
		if(phases!=null)
=======
		if(phases!=null)	
>>>>>>> calpath/arterial
			for(SignalPhase p : phases)
				p.validate();
	}

    @Override
	protected void deploy(double current_time_in_seconds) {

		if(myNode==null)
			return;
<<<<<<< HEAD

		// 0) Advance all phases timers ...........................................
		for(SignalPhase p: phases)
			p.getBulbtimer().advance();
		
		// 1) Update detector stations ............................................
		/*
		for(i=0;i<8;i++)
			phases.get(i).UpdateDetectorStations();
		*/
		
		// 2) Read phases calls .....................................................
/*
		// Update stopline calls
		for(i=0;i<8;i++){
			if( phases.get(i).Recall() ){
				hasstoplinecall[i] = true;
				continue;
			}
			if( phases.get(i).StoplineStation()!=null && phases.get(i).StoplineStation().GotCall() )
				hasstoplinecall[i] = true;
			else
				hasstoplinecall[i] = false;
		}

		// Update approach calls
		for(i=0;i<8;i++){
			if( phases.get(i).ApproachStation()!=null && phases.get(i).ApproachStation().GotCall() )
				hasapproachcall[i] = true;
			else
				hasapproachcall[i] = false;
		}

		// Update conflicting calls
		boolean[] currentconflictcall = new boolean[8];
		for(i=0;i<8;i++)
			currentconflictcall[i] = CheckForConflictingCall(i);
		for(i=0;i<8;i++){
			if(  !hasconflictingcall[i] && currentconflictcall[i] )
				conflictingcalltime[i] = (float)(myNode.getMyNetwork().getSimTime()*3600f);
			hasconflictingcall[i] = currentconflictcall[i];
		}	
*/	

		for(SignalPhase pA: phases)
			pA.updatePermitOpposingHold();
		
		// 3) Update permitted holds ............................................
		for(SignalPhase pA: phases){
			pA.setPermithold(true);
			for(SignalPhase pB: phases)
				if(!isCompatible(pA,pB) && !pB.isPermitopposinghold() )
					pA.setPermithold(false);
=======

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
				if(!isCompatible(phaseA,phaseB) && !phaseB.isPermitopposinghold() )
                    phaseA.setPermithold(false);
>>>>>>> calpath/arterial
		}
		
		// 4) Update signal commands ...................................................
		
		// Throw away conflicting hold pairs 
		// (This is purposely drastic to create an error)
<<<<<<< HEAD
		for(SignalPhase pA: phases)
			if(pA.isHold_requested())
				for(SignalPhase pB: phases)
					if( pB.isHold_requested() && !isCompatible(pA,pB) ){
						pA.setHold_requested(false);
						pB.setHold_requested(false);
=======
		for(SignalPhase phaseA:phases)
			if(phaseA.isHold_requested())
				for(SignalPhase phaseB:phases)
					if( phaseB.isHold_requested() && !isCompatible(phaseA,phaseB) ){
                        phaseA.setHold_requested(false);
                        phaseB.setHold_requested(false);
>>>>>>> calpath/arterial
					}

		// Deal with simultaneous hold and forceoff (RHODES needs this)
<<<<<<< HEAD
		for(SignalPhase pA: phases)
			if( pA.isHold_requested() && pA.isForceoff_requested() )
				pA.setForceoff_requested(false);

//		// Make local relaying copy
//		for(i=0;i<phases.size();i++){
//			hold_approved[i]     = phases[i].isHold_requested();
//			forceoff_approved[i] = phases[i].isForceoff_requested();
//		}
//
//		// No transition if no permission
//		for(i=0;i<phases.size();i++)
//			if( !phases[i].isPermithold() )
//				hold_approved[i] = false;
//
//		// No transition if green time < mingreen
//		for(i=0;i<phases.size();i++)
//			if( phases[i].getBulbColor().compareTo(BulbColor.GREEN)==0  && BeatsMath.lessthan(phases[i].getBulbtimer().getT(), phases[i].getMingreen()) )
//				forceoff_approved[i] = false;
//
//		// collect updated bulb iindications
//        ActuatorSignal.BulbColor [] new_bulb_colors = new ActuatorSignal.BulbColor[phases.size()];
//		for(i=0;i<phases.size();i++)
//            new_bulb_colors[i]=phases[i].get_new_bulb_color(hold_approved[i],forceoff_approved[i]);

//        // deploy
//        mySignal.getImplementor().deploy_bulb_color(myNEMA, new_bulb_colors);
//        bulbcolor = color;
//
//        // update signal state
//        for(i=0;i<phases.length;i++)
//            if(new_bulb_colors[i]!=null)
//                phases[i].bulbcolor = new_bulb_color[i];
=======
		for(SignalPhase phase:phases)
			if( phase.isHold_requested() && phase.isForceoff_requested() )
                phase.setForceoff_requested(false);

		// Make local relaying copy
        for(int i=0;i<phases.length;i++){
            SignalPhase phase = phases[i];
            hold_approved[i] = phase.isHold_requested();
            forceoff_approved[i] = phase.isForceoff_requested();
		}

		// No transition if no permission
        for(int i=0;i<phases.length;i++)
			if( !phases[i].isPermithold() )
				hold_approved[i] = false;

		// No transition if green time < mingreen
        for(int i=0;i<phases.length;i++)
            if( phases[i].getBulbColor().compareTo(BulbColor.GREEN)==0  && BeatsMath.lessthan(phases[i].getBulbtimer().getT(), phases[i].getMingreen()) )
				forceoff_approved[i] = false;

		// collect updated bulb indications
        ActuatorSignal.BulbColor [] new_bulb_colors = new ActuatorSignal.BulbColor[phases.length];
		for(int i=0;i<phases.length;i++){

            SignalPhase phase = phases[i];
            new_bulb_colors[i] = phase.get_new_bulb_color(hold_approved[i],forceoff_approved[i]);
>>>>>>> calpath/arterial

            // set phase color if changed
            if(new_bulb_colors[i]!=null)
                phases[i].bulbcolor = new_bulb_colors[i];
        }

<<<<<<< HEAD
            // Remove serviced commands
		for(SignalPhase pA: phases){
			if(pA.getBulbColor().compareTo(ActuatorSignal.BulbColor.GREEN)==0)
				pA.setHold_requested(false);
			if(pA.getBulbColor().compareTo(ActuatorSignal.BulbColor.YELLOW)==0 || pA.getBulbColor().compareTo(ActuatorSignal.BulbColor.RED)==0 )
				pA.setForceoff_requested(false);
		}
	
//		// Set permissive opposing left turn to yellow
//		// opposing is yellow if I am green or yellow, and I am through, and opposing is permissive
//		// opposing is red if I am red and it is not protected
//		for(i=0;i<phases.length;i++){
//			SignalPhase p = phases[i];
//			SignalPhase o = phases[i].getOpposingPhase();
//			if(o==null)
//				continue;
//			switch(p.getBulbColor()){
//				case GREEN:
//				case YELLOW:
//					if(p.isIsthrough() && o.isPermissive())
//						o.setPhaseColor(ActuatorSignal.BulbColor.YELLOW);
//					break;
//				case RED:
//					if(!o.isProtected())
//						o.setPhaseColor(ActuatorSignal.BulbColor.RED);
//					break;
//			case DARK:
//				break;
//			default:
//				break;
//			}
//		}
		
=======
        // Remove serviced commands
		for(SignalPhase phase: phases){
			if(phase.isGreen())
                phase.setHold_requested(false);
			if(phase.isYellow() || phase.isRed() )
                phase.setForceoff_requested(false);
		}
	
		// Set permissive opposing left turn to yellow
		// opposing is yellow if I am green or yellow, and I am through, and opposing is permissive
		// opposing is red if I am red and it is not protected
		for(SignalPhase phase : phases){
			SignalPhase ophase = phase.getOpposingPhase();
			if(ophase==null)
				continue;
			switch(phase.getBulbColor()){
				case GREEN:
				case YELLOW:
					if(phase.isIsthrough() && ophase.isPermissive())
                        ophase.setPhaseColor(ActuatorSignal.BulbColor.YELLOW);
					break;
				case RED:
					if(!ophase.isProtected())
						ophase.setPhaseColor(ActuatorSignal.BulbColor.RED);
					break;
			case DARK:
				break;
			default:
				break;
			}
		}

        // deploy to dynamics
        implementor().deploy_bulb_color(myNEMA, new_bulb_colors);

>>>>>>> calpath/arterial
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

	protected SignalPhase getPhaseForNEMA(NEMA nema){
<<<<<<< HEAD
		for(SignalPhase p: phases){
=======
		for(SignalPhase p:phases){
>>>>>>> calpath/arterial
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

	public SignalPhase getPhaseByNEMA(ActuatorSignal.NEMA nema){
		if(nema==null)
			return null;
		return nema2phase.get(nema);
	}

	/////////////////////////////////////////////////////////////////////
	// static NEMA methods
	/////////////////////////////////////////////////////////////////////
	
	public static ActuatorSignal.NEMA String2NEMA(String str){
		if(str==null)
			return ActuatorSignal.NEMA.NULL;
		if(str.isEmpty())
			return ActuatorSignal.NEMA.NULL;
		if(!str.startsWith("_"))
			str = "_"+str;
		ActuatorSignal.NEMA nema;
		try{
			nema = ActuatorSignal.NEMA.valueOf(str);
		}
		catch(IllegalArgumentException  e){
			nema = ActuatorSignal.NEMA.NULL;
		}
		return nema;
	}
	
	public static boolean isCompatible(SignalPhase pA,SignalPhase pB){
		ActuatorSignal.NEMA nemaA = pA.getNEMA();
		ActuatorSignal.NEMA nemaB = pB.getNEMA();
		
		if(nemaA.compareTo(nemaB)==0)
			return true;

		if( !pA.isProtected() || !pB.isProtected() )
			return true;

		switch(nemaA){
		case _1:
		case _2:
			if(nemaB.compareTo(NEMA._5)==0 || nemaB.compareTo(NEMA._6)==0)
				return true;
			else
				return false;
		case _3:
		case _4:
			if(nemaB.compareTo(NEMA._7)==0 || nemaB.compareTo(NEMA._8)==0 )
				return true;
			else
				return false;
		case _5:
		case _6:
			if(nemaB.compareTo(NEMA._1)==0 || nemaB.compareTo(NEMA._2)==0 )
				return true;
			else
				return false;
		case _7:
		case _8:
			if(nemaB.compareTo(NEMA._3)==0 || nemaB.compareTo(NEMA._4)==0 )
				return true;
			else
				return false;
		case NULL:
			break;
		default:
			break;
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	public static class Command implements Comparable {
		public ActuatorSignal.CommandType type;
		public ActuatorSignal.NEMA nema;
		public double time;
		public double yellowtime;
		public double redcleartime;

		public Command(ActuatorSignal.CommandType type,ActuatorSignal.NEMA phase,double time){
			this.type = type;
			this.nema = phase;
			this.time = time;
			this.yellowtime = -1f;
			this.redcleartime = -1f;
		}
		
		public Command(ActuatorSignal.CommandType type,ActuatorSignal.NEMA phase,double time,double yellowtime,double redcleartime){
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
			ActuatorSignal.NEMA thistphase = this.nema;
			ActuatorSignal.NEMA thattphase = that.nema;
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
		
	}

	public class PhaseData{
		public NEMA nema;
		public double starttime;
		public double greentime;
		public PhaseData(NEMA nema, double starttime, double greentime){
			this.nema = nema;
			this.starttime = starttime;
			this.greentime = greentime;
		}
	}

}



