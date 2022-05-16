
package controllers.attachments

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, TransactorDetails}
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
  }

}
