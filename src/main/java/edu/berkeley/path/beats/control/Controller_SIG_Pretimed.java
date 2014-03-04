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

package edu.berkeley.path.beats.control;

import java.util.*;

import edu.berkeley.path.beats.actuator.ActuatorSignal;
import edu.berkeley.path.beats.actuator.SignalPhase;

import edu.berkeley.path.beats.simulator.BeatsMath;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Table;

public class Controller_SIG_Pretimed extends Controller {

    // data
    private List<PlanScheduleEntry> plan_schedule;
    private HashMap<Integer,PretimedPlan> plan_map;
    private boolean done;

	// state
	private int cplan_index;        // current plans id
	//private int cperiod;						  // current index to planstarttime and plansequence

	// coordination
	//private ControllerCoordinated coordcont;
	private boolean coordmode = false;					  // true if this is used for coordination (softforceoff only)

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_SIG_Pretimed(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c) {
		super(myScenario,c,Algorithm.SIG_Pretimed);
	}

//	public Controller_SIG_Pretimed(Scenario myScenario) {
//		// TODO Auto-generated constructor stub
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset  / update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
		// must have these

//		if(jaxbc.getTargetActuators()==null ||  jaxbc.getTargetActuators().getTargetActuator()==null  )
//		    return;

		// check all tables are there
		for (String table_name : new String[] {"Plan Sequence","Cycle Length", "Offsets", "Plan List"})
			if (null == getTables().get(table_name))
				return;

        // generate a plan for each entry in the cycle length table
        // add the plans to plan_map
        plan_map = new HashMap<Integer,PretimedPlan>();
        for(Table.Row row : getTables().get("Cycle Length").getRows()){
            int plan_id = Integer.parseInt(row.get_value_for_column_name("Plan ID"));
            double cycle = Double.parseDouble(row.get_value_for_column_name("Cycle Length"));
            PretimedPlan pp = new PretimedPlan(plan_id,cycle);
            plan_map.put(plan_id,pp);
        }

        // read offset table, generate plan intersections
        for(Table.Row row : getTables().get("Offsets").getRows()){
            int plan_id = Integer.parseInt(row.get_value_for_column_name("Plan ID"));
            PretimedPlan pp = plan_map.get(plan_id);
            int intersection_id = Integer.parseInt(row.get_value_for_column_name("Intersection"));
            ActuatorSignal signal = myScenario.get_signal_for_node(intersection_id);
            double offset = Double.parseDouble(row.get_value_for_column_name("Offset"));
            pp.add_intersection_with_offset(intersection_id,signal,offset);
        }

        // read phases/green times
        for(Table.Row row : getTables().get("Plan List").getRows()){
            int plan_id = Integer.parseInt(row.get_value_for_column_name("Plan ID"));
            PretimedPlan pp = plan_map.get(plan_id);
            int intersection_id = Integer.parseInt(row.get_value_for_column_name("Intersection"));
            int movA = Integer.parseInt(row.get_value_for_column_name("Movement A"));
            int movB = Integer.parseInt(row.get_value_for_column_name("Movement B"));
            double green_time = Integer.parseInt(row.get_value_for_column_name("Green Time"));
            pp.add_intersection_stage(intersection_id,movA,movB,green_time);
        }

        // process stage information
        for(PretimedPlan pp : plan_map.values())
            for(IntersectionPlan ip : pp.intersection_plans.values())
                ip.process_stage_info();

        // create plan sequence
        plan_schedule = new ArrayList<PlanScheduleEntry>();
        for(Table.Row row : getTables().get("Plan Sequence").getRows()){
            int plan_id = Integer.parseInt(row.get_value_for_column_name("Plan ID"));
            double start_time = Double.parseDouble(row.get_value_for_column_name("Start Time"));
            plan_schedule.add(new PlanScheduleEntry(start_time, plan_id));
        }
        Collections.sort(plan_schedule);

        // initialize state
        cplan_index = 0;
        done = false;
	}

	@Override
	protected void update() {

		double simtime = getMyScenario().getCurrentTimeInSeconds();

		// time to switch plans .....................................
		if( !done ){
            PlanScheduleEntry next_entry = plan_schedule.get(cplan_index+1);
			if( BeatsMath.greaterorequalthan(simtime,next_entry.start_time) ){
                cplan_index++;
//				if(null == plansequence[cperiod]){
//					// GCG asc.ResetSignals();  GG FIX THIS
//				}
//				if(coordmode)
//					coordcont.SetSyncPoints();
			}
		}

//		if( plansequence[cperiod]==0 )
//			ImplementASC();
//		else
//			plans.get(plansequence[cperiod]).implementPlan(simtime,coordmode);
        plan_schedule.get(cplan_index).plan.implement(simtime);

	}

	@Override
	protected void validate() {

//		super.validate();
//
//		int i;
//
//        // check all tables
//        for (String table_name : new String[] {"Cycle Length", "Offsets", "Plan List", "Plan Sequence"})
//            if (null == getTables().get(table_name)) {
//                BeatsErrorLog.addError("Pretimed controller: no '" + table_name + "' table");
//            }
//
//		// transdelay>=0
//		if(transdelay<0)
//			BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
//
//		// first planstarttime=0
//		if(planstarttime[0]!=0)
//			BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
//
//		// planstarttime is increasing
//		for(i=1;i<planstarttime.length;i++)
//			if(planstarttime[i]<=planstarttime[i-1])
//				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
//
//		// all plansequence ids found
//		for(i=0;i<plansequence.length;i++)
//			if (null == plansequence[i])
//				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
//
//		// all targets are signals
//		for(Actuator act: actuators)
//			if(act.getMyType().compareTo(ScenarioElement.Type.signal)!=0)
//				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
//
//		for (Controller_SIG_Pretimed_Plan pretimed_plan : plans.values())
//			pretimed_plan.validate();

	}

	@Override
	protected void reset() {
		super.reset();
		cperiod = 0;

//		for (Controller_SIG_Pretimed_Plan pretimed_plan : plans.values())
//			pretimed_plan.reset();
	}

	// auxiliary classes: plans list, plans sequence, etc

    protected class PlanScheduleEntry implements Comparable<PlanScheduleEntry> {
        protected PretimedPlan plan;
        protected double start_time;
        public PlanScheduleEntry(double start_time,int plan_id){
            this.start_time = start_time;
            this.plan = plan_map.get(plan_id);
        }
		@Override
		public int compareTo(PlanScheduleEntry other) {
			return Double.compare(this.start_time, other.start_time);
		}
    }

    protected class PretimedPlan {

        protected int id;
        protected double cycle;
        protected HashMap<Integer,IntersectionPlan> intersection_plans;
        public PretimedPlan(int id,double cycle){
            this.cycle = cycle;
            this.id = id;
            this.intersection_plans = new HashMap<Integer,IntersectionPlan>();
        }

        public void add_intersection_with_offset(int intersection_id,ActuatorSignal signal,double offset){
            intersection_plans.put(intersection_id, new IntersectionPlan(this,signal,intersection_id,offset));
        }

        public void add_intersection_stage(int intersection_id,int movA,int movB,double green_time){
            IntersectionPlan ip = intersection_plans.get(intersection_id);
            if(ip==null)
                return;
            ip.add_stage(movA,movB,green_time);
        }

        public void implementPlan(double simtime,boolean coordmode){

            int i;
            double itime;

            // Master clock .............................
            itime =  simtime % cycle;

            // Loop through intersections ...............
            for (IntersectionPlan int_plan : intersection_plans.values()) {

                commandlist.clear();

                // get commands for this intersection
                int_plan.getCommandForTime(itime,commandlist);

                // send command to the signal
                int_plan.signal.set_command(commandlist);

                if( !coordmode ){
                    for(j=0;j<intplan.holdpoint.length;j++)
                        if( reltime==intplan.holdpoint[j] )
                            intplan.signal.IssueHold(j);

                    for(j=0;j<intplan.holdpoint.length;j++)
                        if( reltime==intplan.forceoffpoint[j] )
                            intplan.signal.IssueForceOff(j,intplan.signal.phase[j].actualyellowtime,intplan.signal.phase[j].actualredcleartime);
                }


                // Used for coordinated actuated.
//                if( coordmode ){
//
//                    for( j=0;j<8;j++ ){
//
//
//                            if( !intplan.signal.Phase(j).Protected() )
//                                continue;
//
//                            issyncphase = j==intplan.movA[0] || j==intplan.movB[0];
//
//                            // Non-persisting forceoff request at forceoffpoint
//                            if( reltime==intplan.forceoffpoint[j] )
//                                c.setRequestforceoff(i, j, true);
//
//                            // Hold request for sync phase if
//                            // currently both sync phases are active
//                            // and not reached syncpoint
//                            if( issyncphase &&
//                                c.PhaseA(i)!=null && c.PhaseA(i).MyNEMA()==intplan.movA.get(0) &&
//                                c.PhaseB(i)!=null && c.PhaseB(i).MyNEMA() == intplan.movB.get(0) &&
//                                reltime!= c.Syncpoint(i) )
//                                c.setRequesthold(i, j, true);
//                        }
//                    }
//                }
            }
        }

    }

    protected class IntersectionPlan {

        // reference
        protected PretimedPlan my_plan;
        protected ActuatorSignal my_signal;

        // data
        protected int intersection_id;
        protected double offset;	// offset for the intersection
        protected List<Stage> stages;

        // command
        protected ArrayList<ActuatorSignal.Command> command_sequence;
        protected int curr_command_index;
        protected int nextcommand;
        protected double lastcommandtime;

        public IntersectionPlan(PretimedPlan my_plan,ActuatorSignal my_signal,int intersection_id,double offset){

            this.my_plan = my_plan;
            this.my_signal = my_signal;
            this.intersection_id = intersection_id;
            this.offset = offset;
            this.stages = new ArrayList<Stage>();

            // compute hold and forceoff points ............................................
            double stime, etime;
            int nextstage;
            stime=0;
            for(k=0;k<numstages;k++){

                etime = stime + greentime[k];
                stime = stime + stagelength[k];

                if(k==numstages-1)
                    nextstage = 0;
                else
                    nextstage = k+1;

                if(stime>=totphaselength)
                    stime = 0;

                // do something if the phase changes from this stage to the next
                if(movA[k].compareTo(movA[nextstage])!=0){

                    // force off this stage
                    if(movA[k].compareTo(NEMA.NULL)!=0){
                        pA = signal.getPhaseByNEMA(movA[k]);
                        command.add(new Command(ActuatorSignal.CommandType.forceoff,movA[k],etime,pA.getActualyellowtime(),pA.getActualredcleartime()));
                    }

                    // hold next stage
                    if(movA[nextstage].compareTo(NEMA.NULL)!=0)
                        command.add(new Command(ActuatorSignal.CommandType.hold,movA[nextstage],stime));

                }

                // same for ring B
                if(movB[k].compareTo(movB[nextstage])!=0){
                    if(movB[k].compareTo(NEMA.NULL)!=0){
                        pB = signal.getPhaseByNEMA(movB[k]);
                        command.add(new Command(ActuatorSignal.CommandType.forceoff,movB[k],etime,pB.getActualyellowtime(),pB.getActualredcleartime()));
                    }
                    if(movB[nextstage].compareTo(NEMA.NULL)!=0)
                        command.add(new Command(ActuatorSignal.CommandType.hold,movB[nextstage],stime));
                }

            }

            // Correction: offset is with respect to end of first stage, instead of beginning
            for(ActuatorSignal.Command c : command){
                c.time -= greentime[0];
                if(c.time<0)
                    c.time += plan._cyclelength;
            }

            // sort the commands
            Collections.sort(command);

            lastcommandtime = command.get(command.size()-1).time;


        }

        public void add_stage(int movA,int movB,double green_time){
            stages.add(new Stage(movA,movB,green_time));
        }

        public void process_stage_info(){

            // Set yellowtimes, redcleartimes, stagelength, totphaselength
            int k;
            SignalPhase pA;
            SignalPhase pB;
            double y,r,yA,yB,rA,rB;
            float totphaselength = 0;
            stagelength = new double[numstages];
            for(k=0;k<numstages;k++){

                SignalPhase pA = signal.getPhaseByNEMA(movA[k]);
                SignalPhase pB = signal.getPhaseByNEMA(movB[k]);

                if(pA==null && pB==null)
                    return;

                yA = pA==null ? 0.0 : pA.getYellowtime();
                rA = pA==null ? 0.0 : pA.getRedcleartime();
                yB = pB==null ? 0.0 : pB.getYellowtime();
                rB = pB==null ? 0.0 : pB.getRedcleartime();

                y = Math.max(yA,yB);
                r = Math.max(rA,rB);

                if( InNextStage(pA,k) ){
                    y = yB;
                    r = rB;
                }

                if( InNextStage(pB,k) ){
                    y = yA;
                    r = rA;
                }

                if(pA!=null){
                    pA.setActualyellowtime(y);
                    pA.setActualredcleartime(r);
                }

                if(pB!=null){
                    pB.setActualyellowtime(y);
                    pB.setActualredcleartime(r);
                }

                stagelength[k] = greentime[k]+y+r;
                totphaselength += greentime[k]+y+r;
            }
        }

        protected void update_current_command(double itime){

            double reltime = itime - offset;
            if(reltime<0)
                reltime += my_plan.cycle;

            if(reltime>lastcommandtime)
                return;

            double nexttime = command_sequence.get(nextcommand).time;

            if(nexttime<=reltime){
                while(nexttime<=reltime){
                    commandlist.add(command_sequence.get(nextcommand));
                    nextcommand += 1;
                    if(nextcommand==command_sequence.size()){
                        nextcommand = 0;
                        break;
                    }
                    nexttime = command_sequence.get(nextcommand).time;
                }
            }
        }

    }

    protected class Stage {
        public ActuatorSignal.NEMA movA;
        public ActuatorSignal.NEMA movB;
        public double green_time;
        public Stage(int movAi,int movBi,double green_time){
            this.movA = ActuatorSignal.int_to_nema(movAi);
            this.movB = ActuatorSignal.int_to_nema(movBi);
            this.green_time = green_time;
        }

    }




}
