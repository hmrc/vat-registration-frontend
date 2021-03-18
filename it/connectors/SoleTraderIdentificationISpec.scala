
package connectors

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, Upstream4xxResponse, UpstreamErrorResponse}

class SoleTraderIdentificationISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testJourneyId = "1"
  val createJourneyUrl = "/sole-trader-identification/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/journey/$testJourneyId"
  val connector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in {
        stubPost(createJourneyUrl, CREATED, Json.stringify(Json.obj("journeyId" -> testJourneyId)))

        val res = await(connector.createJourney)

        res mustBe testJourneyId
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in {
        stubPost(createJourneyUrl, CREATED, "{}")

        intercept[InternalServerException] {
          await(connector.createJourney)
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an InternalServerException" in new Setup {
        stubPost(createJourneyUrl, UNAUTHORIZED, "")

        intercept[InternalServerException] {
          await(connector.createJourney)
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        stubPost(createJourneyUrl, IM_A_TEAPOT, "")

        intercept[InternalServerException] {
          await(connector.createJourney)
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
