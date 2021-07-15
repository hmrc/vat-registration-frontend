
package connectors

import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass, GeneralPartnership}
import play.api.libs.json.{JsObject, JsResultException, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class PartnershipIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"
  val createJourneyUrl = "/partnership-identification/api/general-partnership/journey"
  val retrieveDetailsUrl = s"/partnership-identification/api/journey/$testJourneyId"
  val connector: PartnershipIdConnector = app.injector.instanceOf[PartnershipIdConnector]

  val testJourneyConfig: PartnershipIdJourneyConfig = PartnershipIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out"
  )

  val testPostCode = "ZZ1 1ZZ"

  val testPartnershipIdResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testPartnership: GeneralPartnership = GeneralPartnership(
    Some(testSautr),
    Some(testPostCode),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.createJourney(testJourneyConfig))

        res mustBe testJourneyUrl
      }

      "throw a JsResultException when the response JSON doesn't contain the journeyId" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createJourneyUrl, CREATED, "{}")

        intercept[JsResultException] {
          await(connector.createJourney(testJourneyConfig))
        }
      }
    }

    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createJourneyUrl, UNAUTHORIZED, "")

        intercept[InternalServerException] {
          await(connector.createJourney(testJourneyConfig))
        }
      }
    }
  }

  "getDetails" must {
    "return transactor details when Partnership Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testPartnershipIdResponse))
      val res: GeneralPartnership = await(connector.getDetails(testJourneyId))

      res mustBe testPartnership
    }

    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val invalidTransactorJson: JsObject = testPartnershipIdResponse - "identifiersMatch"
      stubGet(retrieveDetailsUrl, OK, Json.stringify(Json.obj("personalDetails" -> invalidTransactorJson)))

      intercept[InternalServerException] {
        await(connector.getDetails(testJourneyId))
      }
    }

    "throw an InternalServerException for any other status" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.getDetails(testJourneyId))
      }
    }
  }
}
