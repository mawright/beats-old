package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.jaxb.OutputRequest;
import edu.berkeley.path.beats.jaxb.SimulationOutput;

import java.util.ArrayList;
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
    // populate / reset / update
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
                                    sim_out.isAggTime(),
                                    sim_out.isAggLinks(),
                                    sim_out.isAggEnsemble(),
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
            cm.update();
    }

    /////////////////////////////////////////////////////////////////////
    // inner class
    /////////////////////////////////////////////////////////////////////

    protected class CumulativeMeasure{

        // value.get(k)[e][l][v] is pm for time k, link l, vehicle type v,ensemble e.
        protected ArrayList<double [][][]> value;
        protected PerformanceMeasure pm;

        // flags for aggregating the various dimensions
        protected boolean agg_time;
        protected boolean agg_links;
        protected boolean agg_ensemble;
        protected boolean agg_vehicle_type;

        // vehicle type index. negative => aggregate over all
        protected int [] vehicle_types;

        protected int num_ensemble;

        // list of links to include
        protected List<Link> link_list;


        public CumulativeMeasure(Scenario scenario,boolean agg_time,boolean agg_links,boolean agg_ensemble,Route route,int vehicle_type_index,PerformanceMeasure pm){

            this.agg_time = agg_time;
            this.agg_links = agg_links;
            this.agg_ensemble = agg_ensemble;

            this.pm = pm;
            this.num_ensemble = scenario.getNumEnsemble();

            // vehicle types to consider
            if(vehicle_type_index<0){
                vehicle_types = new int[scenario.getNumVehicleTypes()];
                for(int i=0;i<scenario.getNumVehicleTypes();i++)
                    vehicle_types[i]=i;
                agg_vehicle_type = true;
            }
            else{
                vehicle_types = new int[1];
                vehicle_types[0] = vehicle_type_index;
                agg_vehicle_type = false;
            }

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
            value = new ArrayList<double [][][]>();
        }

        protected void update(){

            int i,e,v;
            int ii,ee,vv;

            // dimensions of the saved data
            int nE = agg_ensemble ? 1 : num_ensemble;
            int nL = agg_links ? 1 : link_list.size();
            int nV = agg_vehicle_type ? 1 : vehicle_types.length;
            double [][][] X = new double[nE][nL][nV];

            for(i=0;i<link_list.size();i++){
                Link link = link_list.get(i);
                ii = agg_links ? 0 : i;
                for(e=0;e<num_ensemble;e++){
                    ee = agg_ensemble ? 0 : e;
                    for(v=0;v<vehicle_types.length;v++){
                        vv = agg_vehicle_type ? 0 : v;
                        switch(pm){
                            case veh_time:
                                X[ee][ii][vv] = link.getDensityInVeh(e,vehicle_types[v]);
                                break;
                            case veh_distance:
                                X[ee][ii][vv] = link.getOutflowInVeh(e,vehicle_types[v]);
                                break;
                            case delay:
                                X[ee][ii][vv] = link.computeDelayInVeh(e, vehicle_types[v]);
                                break;
                        } 
                    }
                }
            }

            if(agg_time && !value.isEmpty()){
                for(ii=0;ii<nL;ii++)
                    for(ee=0;ee<nE;ee++)
                        for(vv=0;vv<nV;vv++)
                            value.get(0)[ee][ii][vv] += X[ee][ii][vv];
            }
            else
                value.add(X);
        }


    }

}
