
package controllers.attachments

import controllers.BaseControllerISpec
import play.api.http.Status.OK
import play.api.test.Helpers._

class ViewAttachmentControllerISpec extends BaseControllerISpec {
  val url: String = controllers.fileupload.routes.ViewAttachmentController.show.url

  "GET /attachment-details must" must {
    "display the attachment details page" in {

      given().user.isAuthorised()
      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
  }

}
