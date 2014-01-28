package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.jaxb.OutputRequest;
import edu.berkeley.path.beats.jaxb.SimulationOutput;

import java.util.ArrayList;
import java.util.List;

public class PerformanceCalculator {

    protected OutputRequest output_request;
    protected enum PerformanceMeasure {veh_time,veh_distance,delay,travel_time,speed_contour};
    protected Scenario myScenario;
    protected List<CumulativeMeasure> pm_cumulative;

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public PerformanceCalculator(OutputRequest output_request){
        this.output_request = output_request;
    }

    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset / update
    /////////////////////////////////////////////////////////////////////

    public void populate(Scenario myScenario){

        this.myScenario = myScenario;
        pm_cumulative = new ArrayList<CumulativeMeasure>();
        for(SimulationOutput sim_out : output_request.getSimulationOutput()){
            PerformanceMeasure cpm = PerformanceMeasure.valueOf(sim_out.getPerformance());
            switch(cpm){
                case delay:
                case veh_distance:
                case veh_time:
                    pm_cumulative.add(
                            new CumulativeMeasure(
                                    myScenario,
                                    sim_out.getDt(),
                                    sim_out.isAggTime(),
                                    sim_out.isAggLinks(),
                                    sim_out.isAggEnsemble(),
                                    sim_out.isAggVehicleType(),
                                    myScenario.getRouteWithId(sim_out.getRouteId()),
                                    myScenario.getVehicleTypeIndexForId(sim_out.getVehicleTypeId()),
                                    cpm));
                    break;
                case travel_time:
                case speed_contour:
                    break;
            }
        }

    }

    protected void validate() {
        for(CumulativeMeasure cm : pm_cumulative)
            cm.validate(myScenario);
    }

    public void reset() {
        if(pm_cumulative==null)
            return;
        for(CumulativeMeasure cm : pm_cumulative)
            cm.reset();
    }

    protected void update(){
        if(pm_cumulative==null)
            return;
        for(CumulativeMeasure cm : pm_cumulative){

            // update values
            cm.update();

            // write output
            if(!cm.agg_time && myScenario.getClock().getRelativeTimeStep()%cm.dt_steps==0){

                cm.write_output(myScenario.getClock().getT());


            }


        }
    }

    /////////////////////////////////////////////////////////////////////
    // inner class
    /////////////////////////////////////////////////////////////////////

    protected class CumulativeMeasure{

        private int dt_steps;

        // value.get(k)[e][l][v] is pm for time k, link l, vehicle type v,ensemble e.
        protected double [][] value;
        protected PerformanceMeasure pm;

        // flags for aggregating the various dimensions
        protected boolean agg_time;
        protected boolean agg_links;
        protected boolean agg_ensemble;
        protected boolean agg_vehicle_type;

        // vehicle type index. negative => aggregate over all
        protected int vt_index;

        protected int num_ensemble;

        // list of links to include
        protected List<Link> link_list;

        public CumulativeMeasure(Scenario scenario,Double dt,boolean agg_time,boolean agg_links,boolean agg_ensemble,boolean agg_vehicle_type,Route route,int vehicle_type_index,PerformanceMeasure pm){

            this.agg_time = agg_time;
            this.agg_links = agg_links;
            this.agg_ensemble = agg_ensemble;
            this.agg_vehicle_type = agg_vehicle_type;
            this.pm = pm;
            this.num_ensemble = scenario.getNumEnsemble();
            this.vt_index = vehicle_type_index;

            // dt
            dt_steps = dt==null?1:BeatsMath.round(dt/scenario.getSimdtinseconds());

            // links to consider
            link_list = new ArrayList<Link>();
            if(route==null)
                for(edu.berkeley.path.beats.jaxb.Link link : scenario.getNetworkSet().getNetwork().get(0).getLinkList().getLink())
                    link_list.add((Link)link);
            else
                for(Link link : route.ordered_links)
                    link_list.add(link);
        }

        protected void reset(){
            int nE = agg_ensemble ? 1 : num_ensemble;
            int nL = agg_links ? 1 : link_list.size();
            value = new double [nE][nL];
        }

        protected void validate(Scenario scenario){

            if(!agg_vehicle_type && vt_index<0)
                BeatsErrorLog.addError("Performance calculator: Please specify a vehicle type.");

//            if(!BeatsMath.isintegermultipleof(dt,scenario.getSimdtinseconds()))
//
        }

        protected void update(){

            int i,e;
            int ii,ee;

            // dimensions of the saved data
            int nE = agg_ensemble ? 1 : num_ensemble;
            int nL = agg_links ? 1 : link_list.size();
            double [][] X = new double[nE][nL];

            // gather information
            for(i=0;i<link_list.size();i++){
                Link link = link_list.get(i);
                ii = agg_links ? 0 : i;
                for(e=0;e<num_ensemble;e++){
                    ee = agg_ensemble ? 0 : e;
                    switch(pm){
                        case veh_time:
                            X[ee][ii] += agg_vehicle_type ? link.getTotalDensityInVeh(e) :
                                                           link.getDensityInVeh(e,vt_index);
                            break;
                        case veh_distance:
                            X[ee][ii] += agg_vehicle_type ? link.getTotalOutflowInVeh(e) :
                                                           link.getOutflowInVeh(e,vt_index);
                            break;
                        case delay:
                            X[ee][ii] += agg_vehicle_type ? link.computeTotalDelayInVeh(e) :
                                                           link.computeDelayInVeh(e, vt_index);
                            break;
                    }
                }
            }

            // do mean if agg_ensemble
            if(agg_ensemble){
                if(agg_links)
                    X[0][0] /= num_ensemble;
                else{
                    for(i=0;i<link_list.size();i++){
                        ii = agg_links ? 0 : i;
                        X[0][ii] /=  num_ensemble;
                    }
                }
            }

            // add to running aggregate
            for(ii=0;ii<nL;ii++)
                for(ee=0;ee<nE;ee++)
                    value[ee][ii] += X[ee][ii];
            //num_sample++;
        }

        protected void write_output(double time){
            int ii,ee;
            for(ee=0;ee<value.length;ee++){
                String str = String.format("%f\t%d",time,ee);
                for(ii=0;ii<value[ee].length;ii++)
                    str += String.format("\t%f",value[ee][ii]);
                str += "\n";
                System.out.print(str);
            }
            reset();
        }


    }

}
