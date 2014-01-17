package edu.berkeley.path.beats.test.simulator.output

import edu.berkeley.path.beats.control.predictive._
import edu.berkeley.path.beats.simulator.{InitialDensitySet, SplitRatioSet, DemandSet, Network}
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet
import java.lang.Double
import scala.collection.JavaConversions

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 12/14/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
class TestPolicyMaker extends RampMeteringPolicyMaker {
  def givePolicy(net: Network, fd: FundamentalDiagramSet, demand: DemandSet, splitRatios: SplitRatioSet, ics: InitialDensitySet, control: RampMeteringControlSet, dt: Double): RampMeteringPolicySet = {
    val (scen, onramps)= ScenarioConverter.convertScenario(net, fd, demand, splitRatios, ics, control, dt)
    val policySet = new RampMeteringPolicySet
    for (i <- 0 until onramps.size) {
      val policy = new RampMeteringPolicyProfile
      policy.sensorLink = onramps.toList(i)
      policy.rampMeteringPolicy =  JavaConversions.seqAsJavaList(List.fill(scen.simParams.numTimesteps)( .1 * i))
      policySet.profiles.add(policy)
    }
    policySet
  }
}
