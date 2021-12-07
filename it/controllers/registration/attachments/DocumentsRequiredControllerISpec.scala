
package controllers.registration.attachments

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
        .user.isAuthorised
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Other), List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Attached" in {
      given()
        .user.isAuthorised
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Attached), List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Post" in {
      given()
        .user.isAuthorised
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to VAT2 required page when VAT2 is required and method is Post" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](VAT2))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.Vat2RequiredController.show.url)
      }
    }

    "return a redirect to summary page when no attachments are given" in {
      given()
        .user.isAuthorised
        .vatScheme.has("attachments", Json.toJson(Attachments(None, List[AttachmentType]())))

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
        .user.isAuthorised

      val res = buildClient(submitUrl).post(Json.obj())

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.AttachmentMethodController.show.url)
      }
    }
  }
}
