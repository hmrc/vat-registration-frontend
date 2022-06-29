
package controllers.flatratescheme

import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.api.returns.Returns
import models.{FlatRateScheme, GroupRegistration}
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._

class JoinFlatRateSchemeControllerISpec extends ControllerISpec {

  implicit val s4lFrsKey = FlatRateScheme.s4lKey

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
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(10000),
    reclaimVatOnMostReturns = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  ))

  val returnsDataWithBigTurnover: JsValue = Json.toJson[Returns](Returns(
    turnoverEstimate = Some(150001),
    zeroRatedSupplies = Some(10000),
    reclaimVatOnMostReturns = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  ))

  val returnsDataWithoutTurnover: JsValue = Json.toJson[Returns](Returns(
    turnoverEstimate = None,
    zeroRatedSupplies = Some(10000),
    reclaimVatOnMostReturns = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  ))

  "GET /join-flat-rate" must {
    "return OK when the details are in s4l" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns", returnsData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK when the details are in s4l and missing frs flag" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].contains(frsS4LData.copy(joinFrs = None))
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns", returnsData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK when the details are in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .vatScheme.has("returns", returnsData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "redirect to the Attachments Resolver when the turnover is too high" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .vatScheme.has("returns", returnsDataWithBigTurnover)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "redirect to the Attachments Resolver for a Group Registration" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .vatScheme.has("returns", returnsData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        registrationReason = GroupRegistration
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "return INTERNAL_SERVER_ERROR if no estimates data available" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].isEmpty
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .vatScheme.has("returns", returnsDataWithoutTurnover)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /join-flat-rate" must {
    "redirect to the next FRS page if the user answers Yes" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.has("flat-rate-scheme", Json.toJson(frsS4LData))
        .s4lContainer[FlatRateScheme].isUpdatedWith(frsS4LData)
        .vatScheme.isUpdatedWith("flat-rate-scheme", Json.toJson(frsS4LData.copy(joinFrs = Some(true))))
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.flatratescheme.routes.FlatRateController.annualCostsInclusivePage.url)
      }
    }
    "redirect to the next FRS page if the user answers No" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.doesNotHave("flat-rate-scheme")
        .s4lContainer[FlatRateScheme].clearedByKey
        .vatScheme.isUpdatedWith("flat-rate-scheme", Json.toJson(frsS4LData.copy(joinFrs = Some(false))))
        .vatScheme.has("returns", returnsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "return BAD_REQUEST if form binding fails due to missing flat rate condition" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/join-flat-rate").post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
