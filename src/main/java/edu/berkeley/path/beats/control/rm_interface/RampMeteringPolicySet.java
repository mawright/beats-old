package edu.berkeley.path.beats.control.rm_interface;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class RampMeteringPolicySet {
    public List<RampMeteringPolicyProfile> profiles;

    public RampMeteringPolicySet() {
        profiles = new LinkedList<RampMeteringPolicyProfile>();
    }

    @Override
    public String toString() {
        String str = "";
        for (RampMeteringPolicyProfile profile : profiles)
            str += "\t" + profile +"\n";
        return str;
    }
}
