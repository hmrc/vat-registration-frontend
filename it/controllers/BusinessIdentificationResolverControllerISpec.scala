

package controllers

import controllers.registration.applicant.{routes => applicantRoutes}
import controllers.registration.transactor.{routes => transactorRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import play.api.test.Helpers._

class BusinessIdentificationResolverControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.routes.BusinessIdentificationResolverController.resolve.url

  s"GET $url" must {
    List(UkCompany, RegSociety, CharitableOrg).foreach { validPartyType =>
      s"return a redirect to Incorp ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.IncorpIdController.startJourney.url)
        }
      }
    }

    List(Partnership, ScotPartnership, ScotLtdPartnership, LtdPartnership, LtdLiabilityPartnership).foreach { validPartyType =>
      s"return a redirect to Partnership ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PartnershipIdController.startJourney.url)
        }
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { validPartyType =>
      s"return a redirect to Minor Entity ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        }
      }
    }

    "return a redirect to STI if user is a sole trader" in new Setup {
      enable(UseSoleTraderIdentification)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))
        .vatRegistration.honestyDeclaration(testRegId, "true")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
      }
      disable(UseSoleTraderIdentification)
    }
  }
}
