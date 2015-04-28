package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.utils.Double3DMatrix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by gomes on 2/13/14.
 */
public class SplitRatioLogger  {

    Node myNode;
    int dt_steps;
    BufferedWriter writer;

    public SplitRatioLogger(Node node){
        Scenario myScenario = node.myNetwork.getMyScenario();
        myNode = node;
        dt_steps = (int) Math.round(myScenario.split_logger_dt/myScenario.getSimdtinseconds());
        try{
            writer = new BufferedWriter(new FileWriter(myScenario.split_logger_prefix+node.getId()+".txt"));
        } catch (IOException e){
            return;
        }
    }

    public void write(Double3DMatrix splitratio_applied){

        Scenario myScenario = myNode.getMyNetwork().getMyScenario();

        if(myScenario.getClock().getRelativeTimeStep()%dt_steps!=0)
            return;

        int i,j,k;
        for(i=0;i<myNode.getnIn();i++)
            for(j=0;j<myNode.getnOut();j++)
                for(k=0;k<myScenario.getNumVehicleTypes();k++){
                    try{
                        writer.write(
                                String.format("%.1f\t%d\t%d\t%d\t%f\n",
                                        myScenario.getCurrentTimeInSeconds(),
                                        myNode.getInput_link()[i].getId(),
                                        myNode.getOutput_link()[j].getId(),
                                        myScenario.getVehicleTypeIdForIndex(k),
                                        splitratio_applied.get(i,j,k)));
                    } catch(IOException ioe){
                        System.out.println(ioe.getMessage());
                    }
                }
    }

    public void close() throws IOException{
        writer.close();
    }

}
