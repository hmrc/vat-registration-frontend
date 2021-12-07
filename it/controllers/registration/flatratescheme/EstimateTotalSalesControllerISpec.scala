
package controllers.registration.flatratescheme

import itutil.ControllerISpec
import models.{FlatRateScheme, S4LKey, Start}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class EstimateTotalSalesControllerISpec extends ControllerISpec {

  implicit val s4lFrsKey: S4LKey[FlatRateScheme] = FlatRateScheme.s4lKey

  val url: String = controllers.registration.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url

  val testTotalSales = 123456
  val frsData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(testTotalSales),
    overBusinessGoodsPercent = None,
    useThisRate = None,
    frsStart = None,
    categoryOfBusiness = None,
    percent = None
  )
  val fullFrsData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(testTotalSales),
    overBusinessGoodsPercent = Some(true),
    useThisRate = Some(true),
    frsStart = Some(Start(Some(LocalDate.now()))),
    categoryOfBusiness = Some("testCategory"),
    percent = Some(15)
  )

  s"GET $url" must {
    "return OK with prepop when the details are in s4l" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsData)
        .vatScheme.doesNotHave("flat-rate-scheme")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("totalSalesEstimate").attr("value") mustBe testTotalSales.toString
      }
    }

    "return OK with prepop when the details are in the backend" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(fullFrsData)(FlatRateScheme.apiFormat))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("totalSalesEstimate").attr("value") mustBe testTotalSales.toString
      }
    }

    "return OK without prepop" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsData.copy(estimateTotalSales = None)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("totalSalesEstimate").attr("value") mustBe ""
      }
    }
  }

  s"POST $url" must {
    "redirect to the next FRS page when the user submits a valid estimate" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsData.copy(estimateTotalSales = None))
        .s4lContainer[FlatRateScheme].isUpdatedWith(frsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj("totalSalesEstimate" -> testTotalSales.toString))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.FlatRateController.annualCostsLimitedPage.url)
      }
    }

    "update the page with errors when the user submits an invalid estimate" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsData.copy(estimateTotalSales = None))
        .s4lContainer[FlatRateScheme].isUpdatedWith(frsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val invalidEstimates = Seq("", "a", "0", "999999999999999")

      invalidEstimates.map{ estimate =>
        val res: Future[WSResponse] = buildClient(url).post(Json.obj("totalSalesEstimate" -> estimate))

        whenReady(res) { result =>
          result.status mustBe BAD_REQUEST
        }
      }
    }
  }
}