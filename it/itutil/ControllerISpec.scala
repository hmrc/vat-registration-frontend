
package itutil

import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import helpers.RequestsFinder
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures
import support.AppAndStubs

trait ControllerISpec extends IntegrationSpecBase with TestSuite
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureSwitching {

}
