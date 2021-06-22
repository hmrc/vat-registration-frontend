
package controllers

import java.time.LocalDate

import itutil.ControllerISpec
import models.api.returns.Returns
import models.{FRSDateChoice, FlatRateScheme, Start}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._

class FlatRateControllerISpec extends ControllerISpec {

  override def beforeEach(): Unit = {
    super.beforeEach()
    System.clearProperty("feature.system-date")
  }

  override def afterEach(): Unit = {
    super.afterEach()
    System.clearProperty("feature.system-date")
  }

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
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  ))

  val returnsDataWithStartDate: JsValue = Json.toJson[Returns](Returns(
    zeroRatedSupplies = Some(10000),
    reclaimVatOnMostReturns = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = Some(LocalDate.of(2017,1,2))
  ))

  implicit val s4lFrsKey = FlatRateScheme.s4lKey

  val threeWorkingDaysInFuture = LocalDate.of(2018, 5, 29)
  val OneDayBeforeVatStartDate = LocalDate.of(2017,1,1)

  val validFormForStartDatePage = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
    "frsStartDate.day" -> Seq(threeWorkingDaysInFuture.getDayOfMonth.toString),
    "frsStartDate.month" -> Seq(threeWorkingDaysInFuture.getMonthValue.toString),
    "frsStartDate.year" -> Seq(threeWorkingDaysInFuture.getYear.toString))

  val invalidFormForStartDatePage = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
    "frsStartDate.day" -> Seq(OneDayBeforeVatStartDate.getDayOfMonth.toString),
    "frsStartDate.month" -> Seq(OneDayBeforeVatStartDate.getMonthValue.toString),
    "frsStartDate.year" -> Seq(OneDayBeforeVatStartDate.getYear.toString))

  s"frsStartDatePage - ${controllers.routes.FlatRateController.frsStartDatePage().url}" should {
    "return OK and text based on no vat start date provided" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.FlatRateController.frsStartDatePage().url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.html().contains("This must be at least 3 working days in the future") mustBe true
      }
    }
    "return OK and text based on the vat start date already provided by the user" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns", returnsDataWithStartDate)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.FlatRateController.frsStartDatePage().url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.html().contains("This date must be on or after the date the business is registered for VAT.") mustBe true
      }
    }
  }
  s"submitFrsStartDate - ${controllers.routes.FlatRateController.frsStartDatePage().url}" should {
    "return SEE_OTHER when a valid form is posted" in new Setup {
      System.setProperty("feature.system-date", "2018-05-23T01:01:01")

      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsData)
        .s4lContainer[FlatRateScheme].isUpdatedWith(frsS4LData.copy(frsStart = Some(Start(Some(threeWorkingDaysInFuture)))))
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.FlatRateController.frsStartDatePage().url).post(validFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show().url)
      }
    }
    "return BAD_REQUEST when an invalid form is posted when vat Start date is provided (i.e the company is incorporated)" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsDataWithStartDate)
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.FlatRateController.frsStartDatePage().url).post(invalidFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe 400
        val document = Jsoup.parse(res.body)
        document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
      }
    }
    "return INTERNAL_SERVER_ERROR when no returns exists" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.doesNotHave("returns")
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.FlatRateController.frsStartDatePage().url).post(validFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}