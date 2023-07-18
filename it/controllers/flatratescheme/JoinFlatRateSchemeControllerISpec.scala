
package controllers.flatratescheme

import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.api.vatapplication.VatApplication
import models.{FlatRateScheme, GroupRegistration}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class JoinFlatRateSchemeControllerISpec extends ControllerISpec {

  val frsData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(123),
    overBusinessGoodsPercent = Some(true),
    useThisRate = Some(true),
    frsStart = None,
    categoryOfBusiness = None,
    percent = None
  )

  val vatApplication: VatApplication = VatApplication(
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(10000),
    claimVatRefunds = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  )

  val vatApplicationWithBigTurnover: VatApplication = VatApplication(
    turnoverEstimate = Some(150001),
    zeroRatedSupplies = Some(10000),
    claimVatRefunds = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  )

  val vatApplicationWithoutTurnover: VatApplication = VatApplication(
    turnoverEstimate = None,
    zeroRatedSupplies = Some(10000),
    claimVatRefunds = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  )

  "GET /join-flat-rate" must {
    "return OK without prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](None)
        .registrationApi.getSection[VatApplication](Some(vatApplication))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK with prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.getSection[VatApplication](Some(vatApplication))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("value").attr("value") mustBe "true"
      }
    }

    "redirect to the Attachments Resolver when the turnover is too high" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.getSection[VatApplication](Some(vatApplicationWithBigTurnover))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }

    "redirect to the Attachments Resolver for a Group Registration" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.getSection[VatApplication](Some(vatApplication))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        registrationReason = GroupRegistration
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }

    "redirect to the missing answer page if the turnover estimate isn't present" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.getSection[VatApplication](Some(vatApplicationWithoutTurnover))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

  "POST /join-flat-rate" must {
    "redirect to the next FRS page if the user answers Yes" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.replaceSection[FlatRateScheme](frsData.copy(joinFrs = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.flatratescheme.routes.AnnualCostsInclusiveController.show.url)
      }
    }

    "redirect to the Task List if the user answers No" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData))
        .registrationApi.replaceSection[FlatRateScheme](FlatRateScheme(Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.TaskListController.show.url)
      }
    }

    "return BAD_REQUEST if form binding fails due to missing flat rate condition" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient("/join-flat-rate").post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
