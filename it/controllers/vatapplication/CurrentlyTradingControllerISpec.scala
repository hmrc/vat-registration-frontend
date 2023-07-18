

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
  val regStartDateInFuture = LocalDate.now().plusMonths(1)

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(startDate = Some(regStartDate))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(true))))
        .registrationApi.replaceSection[VatApplication](VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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
        .registrationApi.getSection[VatApplication](Some(VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(false))))
        .registrationApi.replaceSection[VatApplication](VatApplication(startDate = Some(regStartDate), currentlyTrading = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe false
        document.select("input[value=false]").hasAttr("checked") mustBe true
      }
    }

    "return OK with 'No' pre-populated if start date in future" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(startDate = Some(regStartDateInFuture), currentlyTrading = Some(false))))
        .registrationApi.replaceSection[VatApplication](VatApplication(startDate = Some(regStartDateInFuture), currentlyTrading = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe false
        document.select("input[value=false]").hasAttr("checked") mustBe true
      }
    }

    "redirect to the missing answer page if the VAT registration date is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(startDate = None, currentlyTrading = Some(false))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
    }
  }

  s"POST $url" must {
    "save to backend when model is complete and redirect to relevant page if yes is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(currentlyTrading = Some(true)))
        .registrationApi.getSection[VatApplication](Some(vatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "throw a missing answer exception if the VAT registration date is missing" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(vatApplication.copy(startDate = None)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
    }

    "save to backend when model is complete and redirect to relevant page if no is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(currentlyTrading = Some(false)))
        .registrationApi.getSection[VatApplication](Some(vatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "save to backend when model is complete with reg start date in future and redirect to relevant page if no is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDateInFuture))

      given
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(currentlyTrading = Some(false)))
        .registrationApi.getSection[VatApplication](Some(vatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = Some(regStartDate))

      given.user
        .isAuthorised()
        .registrationApi.getSection[VatApplication](Some(vatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }

    "redirect to the missing answer page if reg start date is missing" in new Setup {
      val vatApplication = fullVatApplication.copy(startDate = None)
      given.user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(vatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
    }
  }

}
