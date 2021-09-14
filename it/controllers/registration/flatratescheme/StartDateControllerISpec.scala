
package controllers.registration.flatratescheme

import itutil.ControllerISpec
import models.api.returns.Returns
import models.{FRSDateChoice, FlatRateScheme, Start}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._

import java.time.LocalDate

class StartDateControllerISpec extends ControllerISpec {

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
    startDate = Some(LocalDate.of(2017, 1, 2))
  ))

  implicit val s4lFrsKey = FlatRateScheme.s4lKey

  val edrDate = LocalDate.of(2018, 5, 30)
  val oneDayBeforeEdrDate = edrDate.minusDays(1)
  val vatStartDate = LocalDate.of(2017, 1, 2)
  val oneDayBeforeVatStartDate = vatStartDate.minusDays(1)

  def differentDate(date: LocalDate) = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
    "frsStartDate.day" -> Seq(date.getDayOfMonth.toString),
    "frsStartDate.month" -> Seq(date.getMonthValue.toString),
    "frsStartDate.year" -> Seq(date.getYear.toString))

  s"GET /flat-rate-date" should {
    "return OK and text based on no vat start date provided" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold))))
        .vatScheme.has("returns", returnsData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.show().url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.html().contains("This date must be on or within 3 months after the date the business is registered for VAT.") mustBe true
      }
    }
    "return OK and text based on the vat start date already provided by the user" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold))))
        .vatScheme.has("returns", returnsDataWithStartDate)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.show().url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.html().contains("This date must be on or within 3 months after the date the business is registered for VAT.") mustBe true
      }
    }
  }

  s"POST /flat-rate-date" when {
    "on a mandatory journey" should {
      "use the EDR date over the Vat Start Date and redirect when valid data is posted" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[FlatRateScheme].contains(frsS4LData)
          .vatScheme.doesNotHave("flat-rate-scheme")
          .vatScheme.has("returns", returnsDataWithStartDate)
          .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold))))
          .audit.writesAudit()
          .audit.writesAuditMerged()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.submit().url)
          .post(differentDate(edrDate))

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
          val document = Jsoup.parse(res.body)
          document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
        }
      }
      "use the EDR and redirect when valid data is posted" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[FlatRateScheme].contains(frsS4LData)
          .vatScheme.doesNotHave("flat-rate-scheme")
          .vatScheme.has("returns", returnsData)
          .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold))))
          .audit.writesAudit()
          .audit.writesAuditMerged()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.submit().url)
          .post(differentDate(edrDate))

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
          val document = Jsoup.parse(res.body)
          document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
        }
      }
      "return BAD_REQUEST when an invalid form is posted when the date provided is before the EDR" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[FlatRateScheme].contains(frsS4LData)
          .vatScheme.doesNotHave("flat-rate-scheme")
          .vatScheme.has("returns", returnsDataWithStartDate)
          .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold))))
          .audit.writesAudit()
          .audit.writesAuditMerged()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.submit().url)
          .post(differentDate(oneDayBeforeEdrDate))

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
          val document = Jsoup.parse(res.body)
          document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
        }
      }
    }
    "on a voluntary journey" should {
      "use the VAT start date as the lower boundary and redirect when a valid data is posted" in new Setup {
        System.setProperty("feature.system-date", "2018-05-23T01:01:01")

        given()
          .user.isAuthorised
          .s4lContainer[FlatRateScheme].contains(frsS4LData)
          .vatScheme.doesNotHave("flat-rate-scheme")
          .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = voluntaryThreshold))))
          .vatScheme.has("returns", returnsDataWithStartDate)
          .s4lContainer[FlatRateScheme].isUpdatedWith(frsS4LData.copy(frsStart = Some(Start(Some(oneDayBeforeEdrDate)))))
          .audit.writesAudit()
          .audit.writesAuditMerged()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.submit().url)
          .post(differentDate(vatStartDate))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show().url)
        }
      }
      "return INTERNAL_SERVER_ERROR when no returns or threshold data exists" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[FlatRateScheme].contains(frsS4LData)
          .vatScheme.doesNotHave("flat-rate-scheme")
          .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = voluntaryThreshold))))
          .vatScheme.doesNotHave("returns")
          .audit.writesAudit()
          .audit.writesAuditMerged()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient(controllers.registration.flatratescheme.routes.StartDateController.submit().url)
          .post(differentDate(oneDayBeforeVatStartDate))

        whenReady(response) { res =>
          res.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

  }

}
