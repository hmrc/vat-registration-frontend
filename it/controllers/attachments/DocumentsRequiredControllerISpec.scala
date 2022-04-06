
package controllers.attachments

import itutil.ControllerISpec
import models.api._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class DocumentsRequiredControllerISpec extends ControllerISpec {

  val resolveUrl: String = routes.DocumentsRequiredController.resolve.url
  val submitUrl: String = routes.DocumentsRequiredController.submit.url

  s"GET $resolveUrl" must {
    "return a redirect to documents required page when identity evidence is required and method is Other" in {
      given()
        .user.isAuthorised()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Other), List[AttachmentType](IdentityEvidence))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Attached" in {
      given()
        .user.isAuthorised()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Attached), List[AttachmentType](IdentityEvidence))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Post" in {
      given()
        .user.isAuthorised()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to VAT2 required page when VAT2 is required and method is Post" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](VAT2))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat2RequiredController.show.url)
      }
    }

    "return a redirect to VAT51 required page when VAT51 is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType](VAT51))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat51RequiredController.show.url)
      }
    }

    "return a redirect to VAT5L required page when VAT5L is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType](VAT5L))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat5LRequiredController.show.url)
      }
    }

    "return a redirect to Transactor Identity Evidence Required" when {
      "the user is a Transactor and transactor details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType](TransactorIdentityEvidence))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }

      "the user is a Transactor and applicant details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType](IdentityEvidence))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }

      "the user is a Transactor and transactor with applicant details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType](TransactorIdentityEvidence, IdentityEvidence))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }
    }

    "return a redirect to summary page when no attachments are given" in {
      given()
        .user.isAuthorised()
        .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType]())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show.url)
      }
    }
  }

  s"POST $submitUrl" when {
    "redirect to the AttachmentMethod page" in {
      given()
        .user.isAuthorised()

      val res = buildClient(submitUrl).post(Json.obj())

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.AttachmentMethodController.show.url)
      }
    }
  }
}
