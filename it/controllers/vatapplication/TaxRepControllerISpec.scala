

package controllers.vatapplication

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class TaxRepControllerISpec extends ControllerISpec {

  val url = "/tax-representative"

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(hasTaxRepresentative = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'No' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(hasTaxRepresentative = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST $url" must {
    "redirect to Join Flat Rate Scheme page if yes is selected" in new Setup {
      private def verifyRedirect(redirectUrl: String) = {
        given
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](None)
          .s4lContainer[VatApplication].isEmpty
          .s4lContainer[VatApplication].isUpdatedWith(VatApplication(hasTaxRepresentative = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
      }

      enable(TaskList)
      verifyRedirect(controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyRedirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
    }

    "redirect to Join Flat Rate Scheme page if no is selected" in new Setup {
      private def verifyRedirect(redirectUrl: String) = {
        given
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](None)
          .s4lContainer[VatApplication].isEmpty
          .s4lContainer[VatApplication].isUpdatedWith(VatApplication(hasTaxRepresentative = Some(false)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
      }

      enable(TaskList)
      verifyRedirect(controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyRedirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }

}
