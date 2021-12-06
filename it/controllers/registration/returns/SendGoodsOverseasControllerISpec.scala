
package controllers.registration.returns

import itutil.ControllerISpec
import models.api.returns.{OverseasCompliance, Returns}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class SendGoodsOverseasControllerISpec extends ControllerISpec {

  lazy val url: String = routes.SendGoodsOverseasController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(None, None, None, None, None)

  s"GET $url" must {
    "Return OK when there is no value for 'goodsToOverseas' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "Return OK with prepop when there is a value for 'goodsToOverseas' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(true)))))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST $url" must {
    "redirect to the send goods to EU page when the answer is yes" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(true)))))
        .vatScheme.has("threshold-data", Json.toJson(threshold))

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendEUGoodsController.show.url)
      }
    }

    "redirect to the storing goods page when the answer is no" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(false)))))

      val res = buildClient(url).post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.StoringGoodsController.show.url)
      }
    }
  }
}
