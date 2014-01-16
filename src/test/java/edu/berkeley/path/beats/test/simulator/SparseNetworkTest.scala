package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import edu.berkeley.path.beats.simulator._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class SparseNetworkTest extends FunSuite with ShouldMatchers {
  val logger = Logger.getLogger(classOf[SparseNetworkTest])
  test("test mpc network") {
    val scenario = ObjectFactory.createAndLoadScenario("src/test/resources/sparse.xml")
    scenario.initialize(1, 0, 30, 1, "xml", "hi", 1, 1)
    scenario.run()
  }
}
