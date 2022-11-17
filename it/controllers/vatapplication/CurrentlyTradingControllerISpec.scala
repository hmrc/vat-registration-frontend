

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class CurrentlyTradingControllerISpec extends ControllerISpec {

  val url = "/trading-taxable-goods-and-services-date"
  val regStartDate = LocalDate.now().minusMonths(1)

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(startDate = Some(regStartDate)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe true
        document.select("input[value=false]").hasAttr("checked") mustBe false
      }
    }

    "return OK with 'No' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe false
        document.select("input[value=false]").hasAttr("checked") mustBe true
      }
    }
  }

  s"POST $url" must {
    "save to backend when model is complete and redirect to relevant page if yes is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(vatApplication)
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(currentlyTrading = Some(true)))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "save to backend when model is complete and redirect to relevant page if no is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(vatApplication)
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(currentlyTrading = Some(false)))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given.user
        .isAuthorised()
        .registrationApi.getSection[VatApplication](None)
        .s4lContainer[VatApplication].contains(vatApplication)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR if reg start date is missing" in new Setup {
      given.user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(""))

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
