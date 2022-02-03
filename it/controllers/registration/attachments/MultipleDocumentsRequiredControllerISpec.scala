
package controllers.registration.attachments

import itutil.ControllerISpec
import models.{ApplicantDetails, TransactorDetails}
import models.api._
import play.api.libs.json.Json
import play.api.test.Helpers._

class MultipleDocumentsRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.MultipleDocumentsRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence, VAT2))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK for a transactor" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence, VAT2))))
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
