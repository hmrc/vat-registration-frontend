
package itutil

import featuretoggle.FeatureToggleSupport
import helpers.RequestsFinder
import itFixtures.ITRegistrationFixtures
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures
import support.AppAndStubs

trait ControllerISpec extends IntegrationSpecBase with TestSuite
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureToggleSupport {

}
