
package itutil

import featuretoggle.FeatureToggleSupport
import fixtures.ITRegistrationFixtures
import helpers.RequestsFinder
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.AppAndStubs

trait ControllerISpec extends IntegrationSpecBase with TestSuite
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureToggleSupport {

}
