package edu.berkeley.path.beats;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;

/**
 * Created by gomes on 4/16/14.
 */
public class Tester {

    public static void main(String[] args) {


        try{

            String xml_file = "C:\\Users\\gomes\\Desktop\\final_single_vtype.xml";
            Scenario scenario = (Scenario) ObjectFactory.createAndLoadScenario(xml_file);

            double simulation_dt = 5d;
            double start_time = 0d;
            double end_time = 3600d;
            int numEnsembles = 10;

            scenario.initialize(simulation_dt, start_time, end_time, numEnsembles);

            scenario.reset();

            double [][] density_state = new double [288][10];
            for(int i=0;i<288;i++)
                for(int j=0;j<numEnsembles;j++)
                    density_state[i][j] = i+j;

            scenario.setTotalDensity(density_state);


            double [][]  X = scenario.getTotalDensity(-1);

            System.out.println(X);

            // scenario.setTotalDensity(3);

            // Y = scenario.getTotalDensity(-1);
        }
        catch(Exception e){
            System.err.print("errpr");
        }

    }


}
