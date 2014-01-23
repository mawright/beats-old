package edu.berkeley.path.beats.control.predictive_reroute;


import java.util.LinkedList;
import java.util.List;

import edu.berkeley.path.beats.jaxb.Node;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReroutePolicyProfile {
    public Node actuatorNode;
    public List<Double> reroutePolicy; // vehicles / (unit time), each element per simulation timestep

    public ReroutePolicyProfile() {
        reroutePolicy = new LinkedList<Double>();
    }

    public void print() {
        System.out.println(actuatorNode.getNodeName());
        for (Double d : reroutePolicy) {
            System.out.print(d.toString() + ",");
        }
        System.out.println();
    }
}