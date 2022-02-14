package helpers

import config.FrontendAppConfig
import fixtures.BaseA11yFixtures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers

trait A11ySpec extends AnyWordSpec
  with BaseA11yFixtures
  with Matchers
  with GuiceOneAppPerSuite
  with AccessibilityMatchers {

  implicit val request = FakeRequest()
  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val config = app.injector.instanceOf[FrontendAppConfig]

  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

}
