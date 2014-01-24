package edu.berkeley.path.beats.test.simulator.output

import edu.berkeley.path.beats.control.predictive._
import edu.berkeley.path.beats.simulator.{InitialDensitySet, SplitRatioSet, DemandSet, Network}
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet
import java.lang.Double
import scala.collection.JavaConversions
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 12/14/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
class TestPolicyMaker extends RampMeteringPolicyMaker {
  def givePolicy(net: Network, fd: FundamentalDiagramSet, demand: DemandSet, splitRatios: SplitRatioSet, ics: InitialDensitySet, control: RampMeteringControlSet, dt: Double): RampMeteringPolicySet = {
    val pair = AdjointRampMeteringPolicyMaker.convertScenario(net, fd, demand, splitRatios, ics, control, dt)
    val scen = pair.scenario
    val mainline = pair.mainlineStructure
    val policySet = new RampMeteringPolicySet
    mainline.orderedOnramps().toList.zipWithIndex.foreach{ case(onramp , i) =>
      val policy = new RampMeteringPolicyProfile
      policy.sensorLink = onramp
      policy.rampMeteringPolicy =  JavaConversions.seqAsJavaList(List.fill(scen.simParams.numTimesteps)( .1 * i))
      policySet.profiles.add(policy)
    }
    policySet
  }
}
