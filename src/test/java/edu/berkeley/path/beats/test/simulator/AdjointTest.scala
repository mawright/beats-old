package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.beats.control.predictive.{RampMeteringControl, RampMeteringControlSet, AdjointRampMeteringPolicyMaker, ScenarioConverter}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger
import scala.collection.immutable.TreeMap

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class AdjointTest extends FunSuite with ShouldMatchers {
  val logger = Logger.getLogger(classOf[AdjointTest])
  test("woohoo") {
    val scenario = ObjectFactory.createAndLoadScenario("/Users/jdr/Documents/github/net-create/i15s_fix.xml")
    scenario.initialize(5, 0, 18000, 5, "xml", "hi", 1, 1)
//    scenario.run()
    val meters = {
      val mtrs = new RampMeteringControlSet
      val net = scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network]
      val mainline = ScenarioConverter.extractMainline(net)
      val mlNodes = mainline.map {
        _.getBegin_node
      }
      TreeMap(ScenarioConverter.extractOnramps(net).map {
        onramp => mlNodes.indexOf(onramp.getEnd_node) -> onramp
      }: _*).values.foreach {
        or => {
          val meter = new RampMeteringControl
          meter.min_rate = 0.2
          meter.max_rate = 1.0
          meter.link = or
          mtrs.control.add(meter)
        }
      }
      mtrs
    }
    val pm = new AdjointRampMeteringPolicyMaker
    val time_current = 18000
    val pm_dt = 5
    val pm_horizon_steps = 100
    // call policy maker (everything in SI units)
    println("here")
    val policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    policy.print()
  }

}
