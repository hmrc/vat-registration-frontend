
package connectors

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, UnauthorizedException, Upstream4xxResponse, UpstreamErrorResponse}

class SoleTraderIdentificationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"
  val createJourneyUrl = "/sole-trader-identification/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/journey/$testJourneyId"
  val connector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  val testJourneyConfig = SoleTraderIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out"
  )

  "startJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in {
        stubPost(createJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.startJourney(testJourneyConfig))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in {
        stubPost(createJourneyUrl, CREATED, "{}")

        intercept[InternalServerException] {
          await(connector.startJourney(testJourneyConfig))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        stubPost(createJourneyUrl, UNAUTHORIZED, "")

        intercept[UnauthorizedException] {
          await(connector.startJourney(testJourneyConfig))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        stubPost(createJourneyUrl, IM_A_TEAPOT, "")

        intercept[InternalServerException] {
          await(connector.startJourney(testJourneyConfig))
        }
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "return transactor details when STI returns OK" in new Setup {
      stubGet(retrieveDetailsUrl, OK, Json.stringify(Json.obj("personalDetails" -> Json.toJson(testTransactorDetails))))
      val res = await(connector.retrieveSoleTraderDetails(testJourneyId))
      res mustBe testTransactorDetails
    }
    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidTransactorJson = {
        Json.toJson(testTransactorDetails).as[JsObject] - "firstName"
      }
      stubGet(retrieveDetailsUrl, OK, Json.stringify(Json.obj("personalDetails" -> invalidTransactorJson)))

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
    "throw an InternalServerException for any other status" in new Setup {
      stubGet(retrieveDetailsUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
  }

}
