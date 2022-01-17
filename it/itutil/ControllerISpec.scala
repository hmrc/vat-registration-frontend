
package itutil

import featureswitch.core.config.FeatureSwitching
import helpers.RequestsFinder
import fixtures.ITRegistrationFixtures
import org.scalatest.{Suite, TestSuite}
import org.scalatest.concurrent.ScalaFutures
import support.AppAndStubs

trait ControllerISpec extends IntegrationSpecBase with TestSuite
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureSwitching {

}
