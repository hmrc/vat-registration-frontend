
package controllers.fileupload

import itutil.ControllerISpec
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class DocumentUploadErrorControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.DocumentUploadErrorController.show.url

  s"GET $pageUrl" when {
    "the user has 1 or more documents uploaded" must {
      "return OK with the view" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }
    }
  }
}
