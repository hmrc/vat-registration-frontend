
package controllers.attachments

import itutil.ControllerISpec
import models.api._
import models.{ApplicantDetails, TransactorDetails}
import play.api.libs.json.{Format, Json}
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
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence, VAT2))))
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

}
