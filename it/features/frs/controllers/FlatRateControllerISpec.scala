
package features.frs.controllers

import java.time.LocalDate

import features.returns.models.{Returns, Start}
import frs.{FRSDateChoice, FlatRateScheme}
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.AppAndStubs

class FlatRateControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  override def beforeEach(): Unit = {
    super.beforeEach()
    System.clearProperty("feature.system-date")
  }

  override def afterEach(): Unit = {
    super.afterEach()
    System.clearProperty("feature.system-date")
  }

  val frsS4LData = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(123),
    overBusinessGoodsPercent = Some(true),
    useThisRate = Some(true),
    frsStart = None,
    categoryOfBusiness = None,
    percent = None
  )
  val returnsData = Json.toJson[Returns](Returns(
    reclaimVatOnMostReturns = Some(true),
    frequency = None,
    staggerStart = None,
    start = None
  ))
  val returnsDataWithStartDate = Json.toJson[Returns](Returns(
    reclaimVatOnMostReturns = Some(true),
    frequency = None,
    staggerStart = None,
    start = Some(Start(Some(LocalDate.of(2017,1,2))))
  ))
  implicit val s4lFrsKey = FlatRateScheme.s4lkey

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

  s"frsStartDatePage - ${features.frs.controllers.routes.FlatRateController.frsStartDatePage().url}" should {
    "return 200 and text based on no vat start date provided" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url).get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.html().contains("This must be at least 3 working days in the future") mustBe true
      }
    }
    "return 200 and text based on the vat start date already provided by the user" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns", returnsDataWithStartDate)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url).get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.html().contains("This date must be on or after the date the business is registered for VAT.") mustBe true
      }
    }
  }
  s"submitFrsStartDate - ${features.frs.controllers.routes.FlatRateController.frsStartDatePage().url}" should {
    "return 303 when a valid form is posted" in new StandardTestHelpers {
      System.setProperty("feature.system-date", "2018-05-23T01:01:01")

      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsData)
        .s4lContainer.isUpdatedWith[FlatRateScheme](frsS4LData.copy(frsStart = Some(Start(Some(threeWorkingDaysInFuture)))))
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url).post(validFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show().url)
      }
    }
    "return 400 when an invalid form is posted when vat Start date is provided (i.e the company is incorporated)" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.has("returns",returnsDataWithStartDate)
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url).post(invalidFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe 400
        val document = Jsoup.parse(res.body)
        document.html().contains("Enter a date that is on or after the date the company's registered for VAT") mustBe true
      }
    }
    "return 500 when no returns exists" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[FlatRateScheme].contains(frsS4LData)
        .vatScheme.doesNotHave("flat-rate-scheme")
        .vatScheme.doesNotHave("returns")
        .audit.writesAudit()
        .audit.writesAuditMerged()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url).post(validFormForStartDatePage)
      whenReady(response) { res =>
        res.status mustBe 500
      }
    }
  }
}