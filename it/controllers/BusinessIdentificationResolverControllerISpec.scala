

package controllers

import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.{CharitableOrg, EligibilitySubmissionData, Individual, NonUkNonEstablished, Partnership, RegSociety, Trust, UkCompany, UnincorpAssoc}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class BusinessIdentificationResolverControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.routes.BusinessIdentificationResolverController.resolve.url

  s"GET $url" must {
    List(UkCompany, RegSociety, CharitableOrg).foreach { validPartyType =>
      s"return a redirect to Incorp ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.IncorpIdController.startJourney.url)
        }
      }
    }

    List(Partnership).foreach { validPartyType =>
      s"return a redirect to Partnership ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PartnershipIdController.startJourney.url)
        }
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { validPartyType =>
      s"return a redirect to Minor Entity ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        }
      }
    }

    "return a redirect to STI if user is a sole trader" in new Setup {
      enable(UseSoleTraderIdentification)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))
        .vatRegistration.honestyDeclaration(testRegId, "true")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
      }
      disable(UseSoleTraderIdentification)
    }
  }
}