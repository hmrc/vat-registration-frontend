
package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, VatApplication}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SendGoodsOverseasControllerISpec extends ControllerISpec {

  lazy val url: String = routes.SendGoodsOverseasController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(None, None, None, None, None)

  s"GET $url" must {
    "Return OK when there is no value for 'goodsToOverseas' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = None)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)


      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "Return OK with prepop when there is a value for 'goodsToOverseas' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(true))))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST $url" must {
    "redirect to the send goods to EU page when the answer is yes" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendEUGoodsController.show.url)
      }
    }

    "redirect to the storing goods page when the answer is no" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.StoringGoodsController.show.url)
      }
    }

    "return BAD_REQUEST when submitted with missing data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("value" -> ""))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }
}
