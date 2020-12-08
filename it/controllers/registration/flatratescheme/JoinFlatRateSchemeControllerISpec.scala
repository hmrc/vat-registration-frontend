
package controllers.registration.flatratescheme

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.view.ApplicantDetails
import models.{FlatRateScheme, Returns, TurnoverEstimates}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import support.AppAndStubs
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException

class JoinFlatRateSchemeControllerISpec  extends IntegrationSpecBase
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures {

  implicit val s4lFrsKey = FlatRateScheme.s4lkey

  val frsS4LData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(123),
    overBusinessGoodsPercent = Some(true),
    useThisRate = Some(true),
    frsStart = None,
    categoryOfBusiness = None,
    percent = None
  )

  val returnsData: JsValue = Json.toJson[Returns](Returns(
    zeroRatedSupplies = Some(10000),
    reclaimVatOnMostReturns = Some(true),
    frequency = None,
    staggerStart = None,
    start = None
  ))

  val lowTurnoverEstimate = turnOverEstimates.copy(turnoverEstimate = 1000L)

  "GET /join-flat-rate" must {
    "return OK when the details are in s4l" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .vatScheme.has("turnover-estimates-data", Json.toJson(lowTurnoverEstimate))
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when the details are in the backend" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .vatScheme.has("turnover-estimates-data", Json.toJson(lowTurnoverEstimate))
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /join-flat-rate" must {
    "redirect to the next FRS page if the user answers Yes" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .s4lContainer[FlatRateScheme].isUpdatedWith(frsS4LData)
        .vatScheme.isUpdatedWith("flat-rate-scheme", Json.toJson(frsS4LData.copy(joinFrs = Some(true))))
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain (controllers.routes.FlatRateController.annualCostsInclusivePage().url)
      }
    }
    "redirect to the next FRS page if the user answers No" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .vatScheme.doesNotHave("flat-rate-scheme")
        .s4lContainer[FlatRateScheme].cleared
        .vatScheme.isUpdatedWith("flat-rate-scheme", Json.toJson(frsS4LData.copy(joinFrs = Some(false))))
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain (controllers.routes.SummaryController.show().url)
      }
    }
  }

}
