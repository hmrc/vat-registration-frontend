
package controllers.registration.attachments

import itutil.ControllerISpec
import play.api.test.Helpers._

class IdentityEvidenceRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.IdentityEvidenceRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

}
