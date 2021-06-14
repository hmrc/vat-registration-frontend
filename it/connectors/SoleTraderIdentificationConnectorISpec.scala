
package connectors

import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.TransactorDetails
import models.external.incorporatedentityid.{BusinessVerificationStatus, BvPass, SoleTrader}
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, UnauthorizedException}

class SoleTraderIdentificationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"
  val createJourneyUrl = "/sole-trader-identification/api/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"
  val connector: SoleTraderIdentificationConnector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  val testJourneyConfig: SoleTraderIdJourneyConfig = SoleTraderIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out",
    enableSautrCheck = false
  )

  val testSautr = "1234567890"
  val testRegistration = "REGISTERED"
  val testSafeId = "X00000123456789"

  val testSoleTrader: SoleTrader = SoleTrader(
    sautr = testSautr,
    registration = Some(testRegistration),
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
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
      val testSTIResponse: JsObject = Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        ),
        "nino" -> testApplicantNino,
        "dateOfBirth" -> testApplicantDob
      )

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testSTIResponse))
      val res: (TransactorDetails, Option[SoleTrader]) = await(connector.retrieveSoleTraderDetails(testJourneyId))

      res mustBe(testTransactorDetails, None)
    }

    "return transactor details when STI returns OK for a sole trader" in new Setup {
      val testSTIResponse: JsObject = Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        ),
        "nino" -> testApplicantNino,
        "dateOfBirth" -> testApplicantDob,
        "sautr" -> testSautr,
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        ),
        "registration" -> Json.obj(
          "registrationStatus" -> testRegistration,
          "registeredBusinessPartnerId" -> testSafeId
        )
      )

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testSTIResponse))
      val res: (TransactorDetails, Option[SoleTrader]) = await(connector.retrieveSoleTraderDetails(testJourneyId))

      res mustBe(testTransactorDetails, Some(testSoleTrader))
    }

    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidTransactorJson: JsObject = {
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
