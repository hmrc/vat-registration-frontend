
package controllers.attachments

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.test.Helpers._

class TransactorIdentityEvidenceRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.TransactorIdentityEvidenceRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK with person full name when no identifier match" in {
      val personalDetails = Some(testPersonalDetails.copy(identifiersMatch = false))
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails.copy(personalDetails = personalDetails)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(personalDetails = personalDetails)))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
        val document = Jsoup.parse(result.body)
        document.getElementsByTag("h1").text() mustBe
          "You must send us three identity documents for testFirstName testLastName and testFirstName testLastName in order for us to process this VAT application"
      }
    }
  }
}