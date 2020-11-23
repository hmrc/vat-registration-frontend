
package controllers.registration.business

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.{TradingDetails, TradingNameView}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class EuGoodsControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

  class Setup {
    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

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
