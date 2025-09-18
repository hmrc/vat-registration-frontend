/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.flatratescheme

import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.api.vatapplication.VatApplication
import models.{FRSDateChoice, FlatRateScheme}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class StartDateControllerISpec extends ControllerISpec {

  val edrDate: LocalDate = LocalDate.of(LocalDate.now().getYear - 2, 10, 2)
  val oneDayBeforeEdrDate: LocalDate = edrDate.minusDays(1)
  val vatStartDate: LocalDate = LocalDate.of(LocalDate.now().getYear - 2, 1, 2)
  val oneDayBeforeVatStartDate: LocalDate = vatStartDate.minusDays(1)
  val testDate: LocalDate = LocalDate.now().minusYears(1)

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
    zeroRatedSupplies = Some(10000),
    claimVatRefunds = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = None
  )

  val vatApplicationWithStartDate: VatApplication = VatApplication(
    zeroRatedSupplies = Some(10000),
    claimVatRefunds = Some(true),
    returnsFrequency = None,
    staggerStart = None,
    startDate = Some(vatStartDate)
  )

  val eligibilityData: EligibilitySubmissionData = testEligibilitySubmissionData.copy(calculatedDate = Some(edrDate))

  def differentDate(date: LocalDate): Map[String, Seq[String]] = Map(
    "frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
    "frsStartDate.day" -> Seq(date.getDayOfMonth.toString),
    "frsStartDate.month" -> Seq(date.getMonthValue.toString),
    "frsStartDate.year" -> Seq(date.getYear.toString)
  )

  def registrationDate: Map[String, Seq[String]] = Map(
    "frsStartDateRadio" -> Seq(FRSDateChoice.VATDate.toString)
  )

  s"GET /flat-rate-date" should {
    "return OK without prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](None)
        .registrationApi.getSection[VatApplication](Some(vatApplication))
        .registrationApi.getSection[EligibilitySubmissionData](Some(eligibilityData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=VATDate]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=DifferentDate]").hasAttr("checked") mustBe false
      }
    }

    "return OK with prepop for same date as application" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData.copy(frsStart = Some(vatStartDate))))
        .registrationApi.getSection[VatApplication](Some(vatApplicationWithStartDate))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=VATDate]").hasAttr("checked") mustBe true
      }
    }

    "return OK with prepop for different date" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(frsData.copy(frsStart = Some(LocalDate.now()))))
        .registrationApi.getSection[VatApplication](Some(vatApplicationWithStartDate))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=DifferentDate]").hasAttr("checked") mustBe true
      }
    }
  }

  s"POST /flat-rate-date" when {
    "on a regular journey" should {
      "use the Vat Start Date and redirect when valid different date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplicationWithStartDate))
          .registrationApi.getSection[FlatRateScheme](Some(frsData))
          .registrationApi.replaceSection[FlatRateScheme](frsData.copy(frsStart = Some(testDate)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(differentDate(testDate))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "use the Vat Start Date and redirect when same date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplicationWithStartDate))
          .registrationApi.getSection[FlatRateScheme](Some(frsData))
          .registrationApi.replaceSection[FlatRateScheme](frsData.copy(frsStart = Some(vatStartDate)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(registrationDate)

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "update the page with errors when an invalid date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplicationWithStartDate))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(differentDate(oneDayBeforeVatStartDate))

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
          val document = Jsoup.parse(res.body)
          document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
        }
      }
    }

    "on an journey where start date is captured in eligibility (NonUK, TOGC/COLE)" should {
      "use the Calculated Date and redirect when valid different date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplication))
          .registrationApi.getSection[EligibilitySubmissionData](Some(eligibilityData))
          .registrationApi.getSection[FlatRateScheme](Some(frsData))
          .registrationApi.replaceSection[FlatRateScheme](frsData.copy(frsStart = Some(testDate)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(differentDate(testDate))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "use the Calculated Date and redirect when same date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplication))
          .registrationApi.getSection[EligibilitySubmissionData](Some(eligibilityData))
          .registrationApi.getSection[FlatRateScheme](Some(frsData))
          .registrationApi.replaceSection[FlatRateScheme](frsData.copy(frsStart = Some(edrDate)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(registrationDate)

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "update the page with errors when an invalid date is posted" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(vatApplication))
          .registrationApi.getSection[EligibilitySubmissionData](Some(eligibilityData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(controllers.flatratescheme.routes.StartDateController.submit.url)
          .post(differentDate(oneDayBeforeEdrDate))

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
          val document = Jsoup.parse(res.body)
          document.html().contains("Enter a date that is on or after the date the business’s registered for VAT") mustBe true
        }
      }
    }
  }
}
