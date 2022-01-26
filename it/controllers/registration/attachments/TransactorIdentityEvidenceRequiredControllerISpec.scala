
package controllers.registration.attachments

import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.{ApplicantDetails, TransactorDetails}
import play.api.libs.json.Json
import play.api.test.Helpers._

class TransactorIdentityEvidenceRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.TransactorIdentityEvidenceRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

}
