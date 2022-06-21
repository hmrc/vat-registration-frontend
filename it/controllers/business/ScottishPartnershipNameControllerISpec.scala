
package controllers.business

import forms.ScottishPartnershipNameForm
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, NOT_IMPLEMENTED, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class ScottishPartnershipNameControllerISpec extends ControllerISpec {
  "show Scottish Partnership Name page" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Scottish Partnership Name page" should {
    "post to the backend" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/scottish-partnership-name").post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> testCompanyName)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.applicant.routes.PartnershipIdController.startPartnerJourney.url)
    }

    "return BAD_REQUEST for missing partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> "a" * 106))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
