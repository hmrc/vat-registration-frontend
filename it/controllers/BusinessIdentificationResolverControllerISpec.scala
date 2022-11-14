

package controllers

import controllers.grs.{routes => grsRoutes}
import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessIdentificationResolverControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.routes.BusinessIdentificationResolverController.resolve.url

  s"GET $url" must {
    List(UkCompany, RegSociety, CharitableOrg).foreach { validPartyType =>
      s"return a redirect to Incorp ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(grsRoutes.IncorpIdController.startJourney.url)
        }
      }
    }

    List(Partnership, ScotPartnership, ScotLtdPartnership, LtdPartnership, LtdLiabilityPartnership).foreach { validPartyType =>
      s"return a redirect to Partnership ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(grsRoutes.PartnershipIdController.startJourney.url)
        }
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { validPartyType =>
      s"return a redirect to Minor Entity ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(grsRoutes.MinorEntityIdController.startJourney.url)
        }
      }
    }

    "return a redirect to STI if user is a sole trader" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(grsRoutes.SoleTraderIdController.startJourney.url)
      }
    }
  }
}
