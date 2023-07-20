
package controllers.fileupload

import itutil.ControllerISpec
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class DocumentUploadTypeErrorControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.DocumentUploadTypeErrorController.show.url

  s"GET $pageUrl" when {
    "the user has 1 or more documents uploaded" must {
      "return OK with the view" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }
    }
  }
}
