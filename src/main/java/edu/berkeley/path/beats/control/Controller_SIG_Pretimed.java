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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
            double offset = Double.parseDouble(row.get_value_for_column_name("Offset"));
            pp.add_intersection_with_offset(intersection_id,offset);
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
        public void add_intersection_with_offset(int intersection_id,double offset){
            intersection_plans.put(intersection_id, new IntersectionPlan(intersection_id, offset));
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

//            Iterator it = intersection_plans.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                System.out.println(pairs.getKey() + " = " + pairs.getValue());
//                it.remove(); // avoids a ConcurrentModificationException
//            }


            for(i=0;i<intersplan.length;i++){

                commandlist.clear();

                // get commands for this intersection
                intersplan[i].getCommandForTime(itime,commandlist);

                // send command to the signal
                intersplan[i].signal.set_command(commandlist);

                if( !coordmode ){
                    for(j=0;j<intplan.holdpoint.length;j++)
                        if( reltime==intplan.holdpoint[j] )
                            intplan.signal.IssueHold(j);

                    for(j=0;j<intplan.holdpoint.length;j++)
                        if( reltime==intplan.forceoffpoint[j] )
                            intplan.signal.IssueForceOff(j,intplan.signal.phase[j].actualyellowtime,intplan.signal.phase[j].actualredcleartime);
                }


                // Used for coordinated actuated.
			if( coordmode ){

                for( j=0;j<8;j++ ){


                        if( !intplan.signal.Phase(j).Protected() )
                            continue;

                        issyncphase = j==intplan.movA[0] || j==intplan.movB[0];

                        // Non-persisting forceoff request at forceoffpoint
                        if( reltime==intplan.forceoffpoint[j] )
                            c.setRequestforceoff(i, j, true);

                        // Hold request for sync phase if
                        // currently both sync phases are active
                        // and not reached syncpoint
                        if( issyncphase &&
                            c.PhaseA(i)!=null && c.PhaseA(i).MyNEMA()==intplan.movA.get(0) &&
                            c.PhaseB(i)!=null && c.PhaseB(i).MyNEMA() == intplan.movB.get(0) &&
                            reltime!= c.Syncpoint(i) )
                            c.setRequesthold(i, j, true);
                    }
                }
            }
        }


    }

    protected class IntersectionPlan {

        // data
        protected int intersection_id;
        protected double offset;	// offset for the intersection
        protected List<Stage> stages;

        // command
        protected ArrayList<ActuatorSignal.Command> command = new ArrayList<ActuatorSignal.Command>();

        public IntersectionPlan(int intersection_id,double offset){
            this.intersection_id = intersection_id;
            this.offset = offset;
            this.stages = new ArrayList<Stage>();
        }

        public void add_stage(int movA,int movB,double green_time){
            stages.add(new Stage(movA,movB,green_time);
        }
    }

    protected class Stage {
        public SignalPhase phaseA;
        public SignalPhase phaseB;
        public double green_time;
//        public Stage(int movA,int movB,double green_time){
//zdfgdfg
//        }

    }




}
