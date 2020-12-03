
package itutil

import featureswitch.core.config.FeatureSwitching
import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import org.scalatest.concurrent.ScalaFutures
import support.AppAndStubs

trait ControllerISpec extends IntegrationSpecBase
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureSwitching {

}
