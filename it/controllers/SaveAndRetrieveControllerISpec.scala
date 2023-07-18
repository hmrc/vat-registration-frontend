
package controllers

import itutil.ControllerISpec
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SaveAndRetrieveControllerISpec extends ControllerISpec {

  val url = s"/schemes/save-for-later"

  "GET /schemes/save-for-later" must {
    "return SEE_OTHER" in new Setup {
      given
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationProgressSavedController.show.url)
    }
  }

}
