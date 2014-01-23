package edu.berkeley.path.beats.control.predictive_reroute;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReroutePolicySet {
    public List<ReroutePolicyProfile> profiles;

    public ReroutePolicySet() {
        profiles = new LinkedList<ReroutePolicyProfile>();
    }

    public void print() {
        for (ReroutePolicyProfile profile : profiles) {
            profile.print();
        }
    }
}
