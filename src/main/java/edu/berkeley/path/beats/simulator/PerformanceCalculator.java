package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.jaxb.Route;
import edu.berkeley.path.beats.jaxb.RouteLink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Compute performance measures
 *  + either as function of time, or integrated over time
 *      + over whole network, or over a route,
 *          + over all vehicle types, or for some vehicle types, compute
 *              + vehicle hours
 *              + vehicle kilometers
 *              + delay
 *  + for a given route
 *      + speed contour
 *      + for a set of given start times, compute travel time on the route
 */
public class PerformanceCalculator {

    protected enum CumulativePerformanceMeasure {veh_hr,veh_km,delay};
    protected Scenario myScenario;
    protected List<CumulativeMeasure> pm_cumulative;

    /////////////////////////////////////////////////////////////////////
    // construction
    /////////////////////////////////////////////////////////////////////

    public PerformanceCalculator(Scenario S){

        myScenario = S;

        pm_cumulative = new ArrayList<CumulativeMeasure>();
//        pm_cumulative.add(new CumulativeMeasure(true,null,-1,CumulativePerformanceMeasure.veh_hr));
//        pm_cumulative.add(new CumulativeMeasure(true,null,-1,CumulativePerformanceMeasure.veh_km));
//        pm_cumulative.add(new CumulativeMeasure(true,null,-1,CumulativePerformanceMeasure.delay));
    }

    /////////////////////////////////////////////////////////////////////
    // reset / update
    /////////////////////////////////////////////////////////////////////

    public void reset() {
        if(pm_cumulative==null)
            return;
        for(CumulativeMeasure cm : pm_cumulative)
            cm.reset();
    }

    protected void update(){
        if(pm_cumulative==null)
            return;
        for(CumulativeMeasure cm : pm_cumulative)
            cm.update(myScenario);
    }

    /////////////////////////////////////////////////////////////////////
    // private
    /////////////////////////////////////////////////////////////////////

    private static Double [] compute_veh_hr(Scenario scenario,Route route,int vehicle_type_index){
        Double [] x = null;




        return x;
    }

    private static Double [] compute_veh_km(Scenario scenario,Route route,int vehicle_type_index){
        Double [] x = null;
        return x;
    }

    private static Double [] compute_delay(Scenario scenario,Route route,int vehicle_type_index){
        Double [] x = null;
        return x;
    }

    /////////////////////////////////////////////////////////////////////
    // inner class
    /////////////////////////////////////////////////////////////////////

    protected class CumulativeMeasure{
        protected boolean sum_over_time;
        protected Route route;
        protected int vehicle_type_index;
        protected CumulativePerformanceMeasure pm;
        protected ArrayList<Double []> value;
        protected int space_dim;
        protected List<edu.berkeley.path.beats.simulator.Link> link_list;

        /*  sum_over_time=true      => integrate the performance over time
        *   route=null              => do whole network
        *   vehicle_type_index<0    => sum over vehicle types
         */
        public CumulativeMeasure(Scenario scenario,boolean sum_over_time,Route route,int vehicle_type_index,CumulativePerformanceMeasure pm){
            this.sum_over_time = sum_over_time;
            this.route = route;
            this.vehicle_type_index = vehicle_type_index;
            this.pm = pm;
            if(route==null)
                space_dim = 1;
            else
                space_dim = route.getRouteLink().size();
            link_list = new ArrayList<Link>();
            if(route==null)
                for(edu.berkeley.path.beats.jaxb.Link link : scenario.getNetworkSet().getNetwork().get(0).getLinkList().getLink())
                    link_list.add((Link) link);


        }

        protected void reset(){
            value = new ArrayList<Double []>();
        }

        protected void update(Scenario scenario){
            Double [] x = null;
//            switch(pm){
//                case veh_hr:
//                    x = PerformanceCalculator.compute_veh_hr(scenario,sum_over_time,route,vehicle_type_index);
//                    break;
//                case veh_km:
//                    x = PerformanceCalculator.compute_veh_km(scenario, sum_over_time, route, vehicle_type_index);
//                    break;
//                case delay:
//                    x = PerformanceCalculator.compute_delay(scenario, sum_over_time, route, vehicle_type_index);
//                    break;
//            }
            if(x==null)
                return;
            if(x.length!=space_dim)
                return;
            value.add(x);
        }


    }

}
