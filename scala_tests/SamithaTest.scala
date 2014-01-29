package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import scala.collection.JavaConversions._
import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.beats.control.predictive._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger
import edu.berkeley.path.ramp_metering.AdjointRampMetering
import edu.berkeley.path.beats.control.adjoint_glue.AdjointRampMeteringPolicyMaker
import edu.berkeley.path.beats.control.{RampMeteringControlSet, RampMeteringControl}

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class SamithaTest extends FunSuite with ShouldMatchers {
  val logger = Logger.getLogger(classOf[SamithaTest])
  test("samitha") {
    val scenario = ObjectFactory.createAndLoadScenario("data/config/samitha1onramp2.xml")
    scenario.initialize(1,0, 6, 1, "xml", "hi", 1, 1)
    val pm = new AdjointRampMeteringPolicyMaker
    val meters = {
      val mainlineStructure = new AdjointRampMeteringPolicyMaker.MainlineStructure(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network])
      val x = new RampMeteringControlSet
      mainlineStructure.orderedOnramps().toList.foreach { link => {
        val y = new RampMeteringControl
        x.control.add(y)
        y.link = link
        y.min_rate = 0.0
        y.max_rate = 1.0
      }}
      x
    }
    val time_current = 0
    val pm_dt = 1
    val pm_horizon_steps = 6
    // call policy maker (everything in SI units)
    var policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    policy.profiles.toList should have size 1
    policy.profiles.toList.head.rampMeteringPolicy.toList should have size 6
    val scen = AdjointRampMeteringPolicyMaker.convertScenario(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds).scenario
    AdjointRampMetering.noControlCost(scen) should be (8.7 plusOrMinus .01)
    policy.profiles.toList.head.rampMeteringPolicy.toList.zipWithIndex.filter{case(a,b) => List(0,1,3,4,5).contains(b)}.foreach{case (a,b) => a should be (1.0)}
    policy.profiles.toList.head.rampMeteringPolicy.toList.zipWithIndex.filter{case(a,b) => List(2).contains(b)}.foreach{case(a,b) => a.toDouble should be (.1 plusOrMinus .02)}
  }

  test("samitha with initial conditions") {
    val scenario = ObjectFactory.createAndLoadScenario("data/config/samitha1onramp.xml")
    scenario.initialize(1,0, 6, 1, "xml", "hi", 1, 1)
    val pm = new AdjointRampMeteringPolicyMaker
    val meters = {
      val mainlineStructure = new AdjointRampMeteringPolicyMaker.MainlineStructure(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network])
      val x = new RampMeteringControlSet
      mainlineStructure.orderedOnramps().toList.foreach { link => {
        val y = new RampMeteringControl
        x.control.add(y)
        y.link = link
        y.min_rate = 0.0
        y.max_rate = 1.0
      }}
      x
    }
    val time_current = 0
    val pm_dt = 1
    val pm_horizon_steps = 6
    // call policy maker (everything in SI units)
    var policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    policy.profiles.toList should have size 1
    policy.profiles.toList.head.rampMeteringPolicy.toList should have size 6
    val scen = AdjointRampMeteringPolicyMaker.convertScenario(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds).scenario
    AdjointRampMetering.noControlCost(scen) should be (7.9 plusOrMinus .01)
    policy.profiles.toList.head.rampMeteringPolicy.toList.zipWithIndex.filter{case(a,b) => List(1,2,3,4,5).contains(b)}.foreach{case (a,b) => a should be (1.0)}
    policy.profiles.toList.head.rampMeteringPolicy.toList.zipWithIndex.filter{case(a,b) => List(0).contains(b)}.foreach{case(a,b) => a.toDouble should be (.1 plusOrMinus .02)}
  }  
}
