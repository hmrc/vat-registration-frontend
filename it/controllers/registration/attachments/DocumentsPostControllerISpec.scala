
package controllers.registration.attachments

import itutil.ControllerISpec
import play.api.test.Helpers._

class DocumentsPostControllerISpec extends ControllerISpec {

  val showUrl: String = routes.DocumentsPostController.show.url

  s"GET $showUrl" must {
    "return an OK" in {
      given()
        .user.isAuthorised()

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }
}
