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

import edu.berkeley.path.beats.simulator.*;

public class Controller_SIG_Pretimed extends Controller {

    // data
    private List<PlanScheduleEntry> plan_schedule;
    private HashMap<Integer,PretimedPlan> plan_map;
    private boolean done;

	// state
	private int cplan_index;        // current plans id
	//private int cperiod;						  // current index to planstarttime and plansequence

	// coordination
//	private ControllerCoordinated coordcont;
//	private boolean coordmode = false;					  // true if this is used for coordination (softforceoff only)

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_SIG_Pretimed(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c) {
		super(myScenario,c,Algorithm.SIG_Pretimed);
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset  / update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

//		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
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
            String str_movA = row.get_value_for_column_name("Movement A");
            int movA = str_movA!=null ? Integer.parseInt(str_movA) : -1;
            String str_movB = row.get_value_for_column_name("Movement B");
            int movB = str_movB!=null ? Integer.parseInt(str_movB) : -1;
            double green_time = Integer.parseInt(row.get_value_for_column_name("Green Time"));
            pp.add_intersection_stage(intersection_id,movA,movB,green_time);
        }

        // process stage information, generate command lists
        for(PretimedPlan pp : plan_map.values())
            for(IntersectionPlan ip : pp.intersection_plans.values()){
                ip.compute_stage_info();
                ip.generate_command_sequence();
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
        plan_schedule.get(cplan_index).plan.implementPlan(simtime, false);

	}

	@Override
	protected void validate() {

		super.validate();

		int i;

        // check all tables
        for (String table_name : new String[] {"Cycle Length", "Offsets", "Plan List", "Plan Sequence"})
            if (null == getTables().get(table_name)) {
                BeatsErrorLog.addError("Pretimed controller: no '" + table_name + "' table");
            }

		// all plan ids in plan sequence are validand start times non-negative
        for(PlanScheduleEntry pse : plan_schedule){
            if(pse.plan==null)
                BeatsErrorLog.addError("Bad plan id in plan schedule.");
            if(pse.start_time<0)
                BeatsErrorLog.addError("Negative start-time in plan schedule.");
        }

		// all targets are signals
		for(Actuator actuator: actuators)
            if(actuator.get_type().compareTo(Actuator.Type.signal)!=0)
                BeatsErrorLog.addError("Bad actuator type in pretimed controller.");

        // verify plans
		for (PretimedPlan plan : plan_map.values())
            plan.validate();

	}

	@Override
	protected void reset() {
		super.reset();
		for (PretimedPlan plan : plan_map.values())
            for(IntersectionPlan ip:plan.intersection_plans.values())
                ip.reset();
	}

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

        protected void validate() {
            // check cycle is non-negative

            // check that stage lengths add up to cycle, for each phase
        }


        public void implementPlan(double simtime,boolean coordmode){

            double itime;

            // Master clock .............................
            itime =  simtime % cycle;

            // Loop through intersections ...............
            for (IntersectionPlan int_plan : intersection_plans.values()) {

                // get commands for this intersection
                ArrayList<ActuatorSignal.Command> int_commands = int_plan.get_commands_for_time(itime);

                int_plan.my_signal.set_command(int_commands);


//                if( !coordmode ){
//                    for(j=0;j<intplan.holdpoint.length;j++)
//                        if( reltime==intplan.holdpoint[j] )
//                            intplan.signal.IssueHold(j);
//
//                    for(j=0;j<intplan.holdpoint.length;j++)
//                        if( reltime==intplan.forceoffpoint[j] )
//                            intplan.signal.IssueForceOff(j,intplan.signal.phase[j].actualyellowtime,intplan.signal.phase[j].actualredcleartime);
//                }


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
        protected CircularList<Stage> stages;

        // command
        protected CircularIterator command_ptr;
        protected CircularList<ActuatorSignal.Command> command_sequence;
//        protected int curr_command_index;
       // protected int nextcommand;
        //protected double lastcommandtime;

        public IntersectionPlan(PretimedPlan my_plan,ActuatorSignal my_signal,int intersection_id,double offset){
            this.my_plan = my_plan;
            this.my_signal = my_signal;
            this.intersection_id = intersection_id;
            this.offset = offset;
            this.stages = new CircularList<Stage>();
        }

        protected void reset(){
            command_ptr = (CircularIterator) command_sequence.iterator();
        }

        public void add_stage(int movA,int movB,double green_time){
            stages.add(new Stage(movA,movB,green_time));
        }

//        public Stage get_next_stage(Stage this_stage){
//            return get_next_stage(stages.indexOf(this_stage));
//        }
//
//        public Stage get_next_stage(int this_stage_index){
//            int index = (this_stage_index+1) % stages.size();
//            return stages.get(index);
//        }

        public void compute_stage_info(){

            double yA,yB,rA,rB;
            boolean ignore_A,ignore_B;
            double yellow_time;
            double red_clear_time;

            double intersection_time = 0;

            for(Stage stage : stages){

                SignalPhase pA = my_signal.getPhaseByNEMA(stage.movA);
                SignalPhase pB = my_signal.getPhaseByNEMA(stage.movB);

                if(pA==null && pB==null)
                    return;

                Stage next_stage = get_next_stage(stage);
                ignore_A = pA==null || next_stage.has_movement(stage.movA);
                ignore_B = pB==null || next_stage.has_movement(stage.movB);

                yA = ignore_A ? Double.NEGATIVE_INFINITY : pA.getYellowtime();
                rA = ignore_A ? Double.NEGATIVE_INFINITY : pA.getRedcleartime();
                yB = ignore_B ? Double.NEGATIVE_INFINITY : pB.getYellowtime();
                rB = ignore_B ? Double.NEGATIVE_INFINITY : pB.getRedcleartime();

                yellow_time = Math.max(yA,yB);
                red_clear_time = Math.max(rA,rB);

                stage.yellow_time = yellow_time;
                stage.red_time = red_clear_time;
                stage.start_hold_time = intersection_time;
                stage.start_forceoff_time = intersection_time + stage.green_time;

                intersection_time += stage.green_time + yellow_time + red_clear_time;
                intersection_time %= my_plan.cycle;
            }
        }

        public void generate_command_sequence(){

            command_sequence = new CircularList<ActuatorSignal.Command>();

            for(Stage stage : stages){

                boolean have_movA = stage.movA.compareTo(ActuatorSignal.NEMA.NULL)!=0;
                boolean have_movB = stage.movB.compareTo(ActuatorSignal.NEMA.NULL)!=0;

                // holds
                if(have_movA)
                    command_sequence.add(new ActuatorSignal.Command(ActuatorSignal.CommandType.hold,
                                                                    stage.movA,
                                                                    stage.start_hold_time));

                if(have_movB)
                    command_sequence.add(new ActuatorSignal.Command(ActuatorSignal.CommandType.hold,
                                                                    stage.movB,
                                                                    stage.start_hold_time));

                // force-off
                Stage next_stage = stages.get_next(stage);

                if(have_movA && !next_stage.has_movement(stage.movA))
                    command_sequence.add(new ActuatorSignal.Command(ActuatorSignal.CommandType.forceoff,
                                                                    stage.movA,
                                                                    stage.start_forceoff_time,
                                                                    stage.yellow_time,
                                                                    stage.red_time));

                if(have_movB && !next_stage.has_movement(stage.movB))
                    command_sequence.add(new ActuatorSignal.Command(ActuatorSignal.CommandType.forceoff,
                                                                    stage.movB,
                                                                    stage.start_forceoff_time,
                                                                    stage.yellow_time,
                                                                    stage.red_time));
            }

            // Correction: offset is with respect to end of first stage, instead of beginning
            for(ActuatorSignal.Command c : command_sequence){
                c.time -= stages.get(0).green_time;
                if(c.time<0)
                    c.time += my_plan.cycle;
            }

            // sort the commands
            Collections.sort(command_sequence);

            lastcommandtime = command_sequence.get(command_sequence.size()-1).time;

        }

        protected ArrayList<ActuatorSignal.Command> get_commands_for_time(double itime){

            double reltime = itime - offset;
            if(reltime<0)
                reltime += my_plan.cycle;

            if(reltime>lastcommandtime)
                return null;

            double nexttime = ((ActuatorSignal.Command)command_ptr.next()).time;

            ArrayList<ActuatorSignal.Command> new_commands = new ArrayList<ActuatorSignal.Command>();
            while(reltime>=nexttime){
                new_commands.add(command_sequence.get(nextcommand));
                nextcommand += 1;
                if(nextcommand==command_sequence.size()){
                    nextcommand = 0;
                    break;
                }
                nexttime = command_sequence.get(nextcommand).time;
            }

            return new_commands;
        }

    }

    protected class Stage {
        public ActuatorSignal.NEMA movA;
        public ActuatorSignal.NEMA movB;
        public double green_time;
        public double yellow_time;
        public double red_time;
        public double start_hold_time;
        public double start_forceoff_time;
        public Stage(int movAi,int movBi,double green_time){
            this.movA = ActuatorSignal.int_to_nema(movAi);
            this.movB = ActuatorSignal.int_to_nema(movBi);
            this.green_time = green_time;
        }
        public boolean has_movement(ActuatorSignal.NEMA m){
            return m.compareTo(movA)==0 || m.compareTo(movB)==0;
        }
    }

    protected class CircularList<T> extends ArrayList<T> {
        @Override
        public T get(int index) {
            return super.get(index % this.size());
        }
        @Override
        public Iterator<T> iterator() {
            return new CircularIterator(this);
        }
        public T get_next(T x){
            int ind = this.indexOf(x);
            if(ind<0)
                return null;
            return get((ind+1) % size());
        }
    }

    protected class CircularIterator<T> implements Iterator<T>
    {
        private int cur = 0;
        private CircularList<T> coll = null;

        protected CircularIterator(CircularList<T> coll) {
            this.coll = coll;
        }
        public boolean hasNext() {
            return coll.size() > 0;
        }

        public void advance(){
            cur = (cur+1)%coll.size();
        }
        public T next(){        // NOTE: DOES NOT ADVANCE!!
            return coll.get((cur+1)%coll.size());
        }
        public T current(){
            return coll.get(cur);
        }
        public T prev(){
            return coll.get((cur-1)%coll.size());
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
