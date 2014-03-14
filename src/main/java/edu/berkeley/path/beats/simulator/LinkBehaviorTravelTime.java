package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gomes on 3/12/14.
 */
public class LinkBehaviorTravelTime extends LinkBehavior {

    protected List<CellArray> ensemble;     // ensemble of cell arrays

    public LinkBehaviorTravelTime(Link link){
        super(link);

        Scenario scenario = link.myScenario;
        int num_ensemble = scenario.getNumEnsemble();

        double L = link.getLengthInMeters();
        double vf = link.getVfInMPS(0);
        double dt = link.getMyNetwork().getMyScenario().getSimdtinseconds();

        int num_cells = (int) Math.ceil(L/(vf*dt));
        ensemble = new ArrayList<CellArray>();
        for(int e=0;e<num_ensemble;e++)
            ensemble.add(new CellArray(num_cells,scenario.getNumVehicleTypes()));
    }

    /////////////////////////////////////////////////////////////////////
    // LinkBehaviorInterface
    /////////////////////////////////////////////////////////////////////

    // UPDATE

    @Override
    public void update_state(double [][] inflow,double [][] outflow){
//        int e,j;
//        for(e=0;e<myScenario.getNumEnsemble();e++)
//            for(j=0;j<myScenario.getNumVehicleTypes();j++)
//                density[e][j] += inflow[e][j] - outflow[e][j];
    }

    @Override
    public void update_outflow_demand(double external_max_speed, double external_max_flow){
//
//        int numVehicleTypes = myScenario.getNumVehicleTypes();
//
//        double totaldensity;
//        double totaloutflow;
//        FundamentalDiagram FD;
//
//        for(int e=0;e<myScenario.getNumEnsemble();e++){
//
//            FD = myLink.currentFD(e);
//
//            totaldensity = getTotalDensityInVeh(e);
//
//            // case empty link
//            if( BeatsMath.lessorequalthan(totaldensity,0d) ){
//                outflowDemand[e] =  BeatsMath.zeros(numVehicleTypes);
//                continue;
//            }
//
//            // compute total flow leaving the link in the absence of flow control
//            if( totaldensity < FD.getDensityCriticalInVeh() ){
//                totaloutflow = totaldensity * Math.min(FD.getVfNormalized(),external_max_speed);
//            }
//            else{
//                totaloutflow = Math.max(FD._getCapacityInVeh()-FD._getCapacityDropInVeh(),0d);
//                totaloutflow = Math.min(totaloutflow,external_max_speed*FD.getDensityCriticalInVeh());
//            }
//
//            // capacity profile
//            if(myLink.myCapacityProfile!=null)
//                totaloutflow = Math.min( totaloutflow , myLink.myCapacityProfile.getCurrentValue() );
//
//            // flow controller
//            totaloutflow = Math.min( totaloutflow , external_max_flow );
//
//            // flow uncertainty model
//            if(myScenario.isHas_flow_unceratinty()){
//
//                double delta_flow=0.0;
//                double std_dev_flow = myScenario.getStd_dev_flow();
//
//                switch(myScenario.getUncertaintyModel()){
//                    case uniform:
//                        delta_flow = BeatsMath.sampleZeroMeanUniform(std_dev_flow);
//                        break;
//
//                    case gaussian:
//                        delta_flow = BeatsMath.sampleZeroMeanGaussian(std_dev_flow);
//                        break;
//                }
//
//                totaloutflow = Math.max( 0d , totaloutflow + delta_flow );
//                totaloutflow = Math.min( totaloutflow , totaldensity );
//            }
//
//            // split among types
//            double alpha = totaloutflow/totaldensity;
//            for(int j=0;j<myScenario.getNumVehicleTypes();j++)
//                outflowDemand[e][j] = get_density_in_veh(e,j)*alpha;
//
//        }
//
//        return;
    }

    @Override
    public void update_space_supply(){
//        double totaldensity;
//        FundamentalDiagram FD;
//        for(int e=0;e<myScenario.getNumEnsemble();e++){
//            FD = myLink.currentFD(e);
//            totaldensity = getTotalDensityInVeh(e);
//            spaceSupply[e] = FD.getWNormalized()*(FD._getDensityJamInVeh() - totaldensity);
//            spaceSupply[e] = Math.min(spaceSupply[e],FD._getCapacityInVeh());
//
//            // flow uncertainty model
//            if(myScenario.isHas_flow_unceratinty()){
//                double delta_flow=0.0;
//                double std_dev_flow = myScenario.getStd_dev_flow();
//                switch(myScenario.getUncertaintyModel()){
//                    case uniform:
//                        delta_flow = BeatsMath.sampleZeroMeanUniform(std_dev_flow);
//                        break;
//
//                    case gaussian:
//                        delta_flow = BeatsMath.sampleZeroMeanGaussian(std_dev_flow);
//                        break;
//                }
//                spaceSupply[e] = Math.max( 0d , spaceSupply[e] + delta_flow );
//                spaceSupply[e] = Math.min( spaceSupply[e] , FD._getDensityJamInVeh() - totaldensity);
//            }
//        }
    }

    // GET / SET / RESET DENSITY

    @Override
    public double get_density_in_veh(int ensemble_index, int vehicletype_index) throws IndexOutOfBoundsException {
        return ensemble.get(ensemble_index).get_density_for_vehicletype_in_veh(vehicletype_index);
    }

    @Override
    public boolean set_density_in_veh(int e,double [] d){
        if(e<0 || e>=ensemble.size())
            return false;
        if(d.length!=myScenario.getNumVehicleTypes())
            return false;
        if(!BeatsMath.all_non_negative(d))
            return false;
        for(int v=0;v<d.length;v++)
            ensemble.get(e).set_density_in_veh(d);
        return true;
    }

    @Override
    public void reset_density() {
        for(CellArray ca : ensemble)
            ca.reset();
    }

    // COMPUTE

    @Override
    public double compute_speed_in_mps(int ensemble){
//        try{
//            if(myScenario.getClock().getRelativeTimeStep()==0)
//                return Double.NaN;
//
//            double totaldensity = BeatsMath.sum(density[ensemble]);
//            double speed = BeatsMath.greaterthan(totaldensity,0d) ?
//                    BeatsMath.sum(myLink.outflow[ensemble])/totaldensity :
//                    myLink.currentFD(ensemble).getVfNormalized();
//            return speed * myLink._length / myScenario.getSimdtinseconds();
//        } catch(Exception e){
//            return Double.NaN;
//        }
        return 0;
    }

    @Override
    public double compute_delay_in_veh(int e, int vt_index){
//        double n = get_density_in_veh(ensemble, vt_index);
//        double f = myLink.getOutflowInVeh(ensemble, vt_index);
//        double vf = myLink.getNormalizedVf(ensemble);
//        return Math.max(0d,vf*n-f);
        return 0;
    }

    /////////////////////////////////////////////////////////////////////
    // Cell Array class
    /////////////////////////////////////////////////////////////////////

    private class CellArray{
        public ArrayList<Cell> cell_array;
        public CellArray(int numcell,int num_veh_types){
            cell_array = new ArrayList<Cell>();
            for(int i=0;i<numcell;i++)
                cell_array.add(new Cell(num_veh_types));
        }

        // x is an array over vehicle types
        public boolean set_density_in_veh(double [] x){
            if(cell_array.isEmpty())
                return false;
            double [] x_per_cell = BeatsMath.times(x,1/cell_array.size());
            for(Cell cell : cell_array)
                cell.set_vehicles(x_per_cell);
            return true;
        }
        protected double get_density_for_vehicletype_in_veh(int vtind){
            double val = 0d;
            for(Cell cell : cell_array)
                val += cell.get_density_for_vehicletype_in_veh(vtind);
            return val;
        }
        protected void reset(){
            for(Cell cell : cell_array)
                cell.reset();
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Cell class
    /////////////////////////////////////////////////////////////////////

    private class Cell {
        public double [] n;        // [ve] for each vehicle type
        public Cell(int nVT){
            n=BeatsMath.zeros(nVT);
        }
        protected void set_vehicles(double [] x){
            for(int i=0;i<x.length;i++)
                n[i]=x[i];
        }
        protected double get_density_for_vehicletype_in_veh(int vtind){
            return n[vtind];
        }
        protected void reset(){
            int nVT = n.length;
            n = BeatsMath.zeros(nVT);
        }
    }

}
