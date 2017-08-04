package connectors

import support.AppAndStubs
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class AuthConnectorISpec extends UnitSpec with AppAndStubs {
// getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] =
//     OptionT(http.GET[Option[String]](s"$vatRegUrl/vatreg/$regId/acknowledgement-reference").recover{
//       case e: Exception => throw logResponse(e, className, "getAckRef")
//     })

  "getAckRef" should {
    "return None when no ack ref returned from backend" in {
      given()
        .agentAdmin("ABCDEF123456", "12345")
        .isLoggedIn()
        .andIsNotSubscribedToAgentServices()

      await(newVatRegConnector().getAckRef()) shouldBe None
    }

    "return the ARN when the agent has an HMRC-AS-AGENT enrolment" in {
      given()
        .agentAdmin("ABCDEF123456", "12345")
        .isLoggedIn()
        .andIsSubscribedToAgentServices()

      await(newVatRegConnector().getAckRef()) shouldBe Some("ABCDEF123456")
    }
  }

  def newVatRegConnector() = app.injector.instanceOf(classOf[VatRegistrationConnector])
}

