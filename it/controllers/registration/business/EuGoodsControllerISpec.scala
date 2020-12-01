
package controllers.registration.business

import itutil.ControllerISpec
import models.{TradingDetails, TradingNameView}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class EuGoodsControllerISpec extends ControllerISpec {

  "GET /trade-goods-outside-eu" must {
    "return OK when trading details aren't stored" in new Setup {
      given
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/trade-goods-outside-eu").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when trading details are stored in S4L" in new Setup {
      given
        .user.isAuthorised
        .s4lContainer[TradingDetails].contains(TradingDetails(euGoods = Some(true)))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/trade-goods-outside-eu").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when trading details are stored in the backend" in new Setup {
      given
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .vatScheme.has("trading-details", Json.toJson(TradingDetails(tradingNameView = Some(TradingNameView(false, None)), Some(true)))(TradingDetails.writes))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/trade-goods-outside-eu").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /trade-goods-outside-eu" must {
    "redirect to the next page" in new Setup {
      given
        .user.isAuthorised
        .s4lContainer[TradingDetails].contains(TradingDetails(tradingNameView = Some(TradingNameView(false, None))))
        .s4lContainer[TradingDetails].cleared
        .vatScheme.doesNotHave("trading-details")
        .vatScheme.isUpdatedWith("tradingDetails", Json.toJson(TradingDetails(tradingNameView = Some(TradingNameView(false, None)), Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/trade-goods-outside-eu").post(Json.obj("euGoodsRadio" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.ZeroRatedSuppliesController.show().url)
      }
    }
  }

}
