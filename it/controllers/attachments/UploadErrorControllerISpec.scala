
package controllers.attachments

import controllers.BaseControllerISpec
import itFixtures.ITRegistrationFixtures
import play.api.test.Helpers._

class UploadErrorControllerISpec extends BaseControllerISpec with ITRegistrationFixtures {

  val url: String => String = controllers.fileupload.routes.UploadErrorController.show(_).url
  val testReference = "testReference"


  "GET /upload-error/VAT2" must {
    "show the upload error page" in {
      given().user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List())

      val res = await(buildClient(url("VAT2")).get())

      res.status mustBe OK

    }
  }

}
