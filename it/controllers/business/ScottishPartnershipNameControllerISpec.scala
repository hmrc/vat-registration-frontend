
package controllers.business

import forms.ScottishPartnershipNameForm
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Partnership, ScotPartnership}
import models.{ApplicantDetails, Entity}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Format
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
        .registrationApi.getSection[Entity](Some(Entity(Some(testPartnership), ScotPartnership, Some(true), None)), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "with pre-pop for existing data and return OK" in new Setup {
      private val scottishPartnershipName = "updated name"

      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), ScotPartnership, Some(true), Some(scottishPartnershipName))), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("scottishPartnershipName").attr("value") mustBe scottishPartnershipName
      }
    }
  }

  "submit Scottish Partnership Name page" should {
    "post to the backend" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)

      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testPartnership))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))
        .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None)), idx = Some(1))
        .registrationApi.replaceSection[Entity](Entity(None, ScotPartnership, Some(true), Some(testCompanyName)), idx = Some(1))

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
