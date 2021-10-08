
package controllers.registration.attachments

import itutil.ControllerISpec
import models.api.{Attached, AttachmentType, Attachments, IdentityEvidence, Other, Post}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class DocumentsRequiredControllerISpec extends ControllerISpec {

  val resolveUrl: String = routes.DocumentsRequiredController.resolve().url
  val showUrl: String = routes.DocumentsRequiredController.show().url

  s"GET $resolveUrl" must {
    "return a redirect to documents required page when identity evidence is required and method is Other" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Other),List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.DocumentsRequiredController.show().url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Attached" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Attached),List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.DocumentsRequiredController.show().url)
      }
    }

    "return a redirect to documents required page when identity evidence is required and method is Post" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post),List[AttachmentType](IdentityEvidence))))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.attachments.routes.DocumentsRequiredController.show().url)
      }
    }

    "return a redirect to summary page when no attachments are given" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(None ,List[AttachmentType]())))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show().url)
      }
    }
  }

  s"GET $showUrl" must {
    "return an OK" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }
}
