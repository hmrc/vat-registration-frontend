/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.vatapplication

import itutil.ControllerISpec
import models.DateSelection.{calculated_date, specific_date}
import models.api.vatapplication.VatApplication
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, DateSelection}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.test.Helpers._

import java.time.LocalDate

class MandatoryStartDateControllerISpec extends ControllerISpec {

  val url: String = routes.MandatoryStartDateController.show.url
  val continueUrl: String = routes.MandatoryStartDateController.continue.url

  s"GET $url" when {
    "the calculated date is within 4 years" must {
      "return the view" in {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        val res = buildClient(url).get()

        whenReady(res) { result =>
          val doc = Jsoup.parse(result.body)
          result.status mustBe OK
          doc.title() mustBe "When would you like your VAT registration date to start? - Register for VAT - GOV.UK"
        }
      }

      "return the view with calculated date prepop" in {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = Some(testCalculatedDate))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        val res = buildClient(url).get()

        whenReady(res) { result =>
          val doc = Jsoup.parse(result.body)
          result.status mustBe OK
          doc.title() mustBe "When would you like your VAT registration date to start? - Register for VAT - GOV.UK"
          doc.select(s"input[value=${calculated_date.toString}]").hasAttr("checked") mustBe true
        }
      }

      "return the view with earlier date prepop" in {
        val startDate = LocalDate.now().minusYears(1)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = Some(startDate))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        val res = buildClient(url).get()

        whenReady(res) { result =>
          val doc = Jsoup.parse(result.body)
          result.status mustBe OK
          doc.title() mustBe "When would you like your VAT registration date to start? - Register for VAT - GOV.UK"
          doc.select(s"input[value=${specific_date.toString}]").hasAttr("checked") mustBe true
          doc.select(s"input[id=date.day]").`val`() mustBe startDate.getDayOfMonth.toString
          doc.select(s"input[id=date.month]").`val`() mustBe startDate.getMonthValue.toString
          doc.select(s"input[id=date.year]").`val`() mustBe startDate.getYear.toString
        }
      }
    }

    "the calculated date is more than 4 years in the past" must {
      "return the view" in {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(calculatedDate = Some(LocalDate.now().minusYears(5)))))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        val res = buildClient(url).get()

        whenReady(res) { result =>
          val doc = Jsoup.parse(result.body)
          result.status mustBe OK
          doc.title() mustBe "VAT registration start date - Register for VAT - GOV.UK"
        }
      }
    }

    "Return INTERNAL_SERVER_ERROR when the user is not authenticated" in {
      given()
        .user.isNotAuthorised

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST $url" must {
    "redirect to tasklist after saving an earlier date" in {
      val yesterday = LocalDate.now().minusDays(1)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
        .registrationApi.replaceSection[VatApplication](fullVatApplication.copy(startDate = Some(yesterday)))
        .s4lContainer[VatApplication].clearedByKey

      val res = buildClient(url).post(Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> yesterday.getDayOfMonth.toString,
        "date.month" -> yesterday.getMonthValue.toString,
        "date.year" -> yesterday.getYear.toString
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
    "redirect to tasklist after saving the calculated date" in {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
        .registrationApi.replaceSection[VatApplication](fullVatApplication.copy(startDate = Some(testCalculatedDate)))
        .s4lContainer[VatApplication].clearedByKey

      val res = buildClient(url).post(Map(
        "value" -> DateSelection.calculated_date.toString
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
    "return the page with errors for invalid answers" in {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
        .registrationApi.replaceSection[VatApplication](fullVatApplication.copy(startDate = Some(testCalculatedDate)))
        .s4lContainer[VatApplication].clearedByKey

      val res = buildClient(url).post(Map(
        "value" -> DateSelection.specific_date.toString
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

  s"POST $continueUrl" must {
    "redirect to task list" in {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = None)))
        .registrationApi.replaceSection[VatApplication](fullVatApplication.copy(startDate = Some(testCalculatedDate)))
        .s4lContainer[VatApplication].clearedByKey

      val res = buildClient(continueUrl).post(Map[String, String]())

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

}
