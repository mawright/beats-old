package edu.berkeley.path.beats._experiments;

import edu.berkeley.path.beats.Runner;

/**
 * Created by gomes on 11/6/2014.
 */
public class mpc_demo {

    public static void main( String [] args ){
        String cfg_folder = "C:\\Users\\gomes\\code\\L0\\L0-mpc-demo\\data\\";
        String prefix = "210W_pm_cropped_L0_";
        String [] controllers = {"lp"}; //,"nocontrol","alinea","adjoint"};
        for(String cntrl : controllers)
            Runner.run_simulation( cfg_folder + prefix + cntrl + ".properties" );
    }

}
