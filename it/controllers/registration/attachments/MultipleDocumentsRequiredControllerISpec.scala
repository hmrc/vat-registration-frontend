
package controllers.registration.attachments

import itutil.ControllerISpec
import models.api._
import play.api.libs.json.Json
import play.api.test.Helpers._

class MultipleDocumentsRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.MultipleDocumentsRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(Post), List[AttachmentType](IdentityEvidence, VAT2))))

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

}
