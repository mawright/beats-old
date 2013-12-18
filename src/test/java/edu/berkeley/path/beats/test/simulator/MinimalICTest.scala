package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import scala.collection.JavaConversions._
import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.beats.control.predictive.{RampMeteringControl, RampMeteringControlSet, AdjointRampMeteringPolicyMaker, ScenarioConverter}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger
import scala.collection.immutable.TreeMap
import edu.berkeley.path.ramp_metering.AdjointRampMetering

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class MinimalICTest extends FunSuite with ShouldMatchers {
  val logger = Logger.getLogger(classOf[MinimalICTest])
  test("samitha") {
    val scenario = ObjectFactory.createAndLoadScenario("/Users/jdr/Documents/github/beats/data/config/minimal.xml")
    scenario.initialize(1,0, 6, 1, "xml", "hi", 1, 1)
    println(scenario.getNetworkSet.getNetwork.head.asInstanceOf[Network].getListOfLinks.toList.head.asInstanceOf[Link].getDensityInVeh(0))
    val ic_densities = scenario.gather_current_densities
    println(ic_densities.getDensity.toList.map{_.getContent}.mkString("\n"))
  }
}
