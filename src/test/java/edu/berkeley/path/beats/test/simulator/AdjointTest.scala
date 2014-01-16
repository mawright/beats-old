package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger
import edu.berkeley.path.beats.control.predictive.{RampMeteringControl, RampMeteringControlSet, AdjointRampMeteringPolicyMaker}
import edu.berkeley.path.beats.simulator.Network
import edu.berkeley.path.beats.simulator.ObjectFactory
import scala.collection.JavaConversions._

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
  test("Testing proper fallback procedures for converting ramp metering values to flow rates") {
    val scenario = ObjectFactory.createAndLoadScenario("src/test/resources/15sc.xml")
    scenario.initialize(5, 0, 18000, 5, "xml", "hi", 1, 1)
    val maxRate = 1000.0
    val pm = new AdjointRampMeteringPolicyMaker
    val meters = {
      val mainlineStructure = new AdjointRampMeteringPolicyMaker.MainlineStructure(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network])
      val x = new RampMeteringControlSet
      mainlineStructure.orderedOnramps().toList.foreach { link => {
        val y = new RampMeteringControl
        x.control.add(y)
        y.link = link
        y.min_rate = 0.0
        y.max_rate = maxRate
      }}
      x
    }
    val time_current = 18000
    val pm_dt = 5
    val pm_horizon_steps = 20
    // call policy maker (everything in SI units)
    var policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), scenario.predict_demands(time_current, pm_dt, pm_horizon_steps), scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    val rMax = 5.0 / 9
    val lanes = List(2,2,2,1,2,2,2,2)
    policy.profiles.toList.zip(lanes).foreach{case(prof , lane)=> {
      prof.rampMeteringPolicy.toList.map{_.toDouble / lane}.foreach{
        v => {
          v should be <= (maxRate)
          v should be <= (rMax * 1.00001)
          v should be >= (.2)
        }
      }
    }}
    var demands = scenario.predict_demands(time_current, pm_dt, pm_horizon_steps)
    scala.collection.convert.WrapAsScala.asScalaIterator(demands.getDemandProfile.iterator()).foreach{prof => {
      scala.collection.convert.WrapAsScala.asScalaIterator(prof.getDemand.iterator()).foreach{demand => {
        val nzeros = demand.getContent.split(",").size
        demand.setContent(List.fill(nzeros)("0").mkString(","))
      }}
    }}
    policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), demands, scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    policy.profiles.toList.zip(lanes).foreach{case(prof , lane)=> {
      prof.rampMeteringPolicy.toList.map{_.toDouble / lane}.foreach{
        v => {
          v should be <= (rMax * 1.00001)
          v should be >= (rMax * .99)
        }
      }
    }}
    demands = scenario.predict_demands(time_current, pm_dt, pm_horizon_steps)
    scala.collection.convert.WrapAsScala.asScalaIterator(demands.getDemandProfile.iterator()).foreach{prof => {
      scala.collection.convert.WrapAsScala.asScalaIterator(prof.getDemand.iterator()).foreach{demand => {
        val nzeros = demand.getContent.split(",").size
        demand.setContent(List.fill(nzeros)(".02").mkString(","))
      }}
    }}
    policy = pm.givePolicy(scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network], scenario.gather_current_fds(time_current), demands, scenario.predict_split_ratios(time_current, pm_dt, pm_horizon_steps), scenario.gather_current_densities, meters, scenario.getSimdtinseconds)
    policy.profiles.toList.zip(lanes).foreach{case(prof , lane)=> {
      prof.rampMeteringPolicy.toList.map{_.toDouble / lane}.foreach{
        v => {
          v should be <= (rMax * 1.00001)
          v should be >= (rMax * .99)
        }
      }
    }}
  }

}
