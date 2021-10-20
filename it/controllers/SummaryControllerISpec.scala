/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import itutil.ControllerISpec
import models.{Director, Email, SicAndCompliance}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.collection.JavaConverters._
import scala.concurrent.Future

class SummaryControllerISpec extends ControllerISpec {

  "GET Summary page" should {
    "display the summary page correctly" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.contains(fullVatScheme)
        .s4lContainer[SicAndCompliance].cleared
        .vatRegistration.storesNrsPayload(testRegId)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("eligibility-data", fullEligibilityDataJson)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/check-confirm-answers").get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.title() mustBe "Check your answers before sending your application - Register for VAT - GOV.UK"
        document.select("h1").first().text() mustBe "Check your answers before sending your application"

        val cyaRows = document
          .getElementsByTag("dl").last()
          .getElementsByTag("div")
          .asScala.toList.map { element =>
          (
            element.getElementsByTag("dt").first().text(),
            element.getElementsByTag("dd").first().text()
          )
        }

        val expectedCyaRows: List[(String, String)] = List(
          ("Company registration number", testCrn),
          ("Unique Taxpayer Reference number", testCtUtr),
          ("First Name", testFirstName),
          ("Last Name", testLastName),
          ("National Insurance number", testApplicantNino),
          ("Date of birth", "1 January 2020"),
          ("Role in the business", Director.toString),
          ("Former name", "New Name Cosmo"),
          ("Date former name changed", "12 July 2000"),
          ("Email address", "test@t.test"),
          ("Telephone number", testApplicantPhone),
          ("Home address", "Testline1 Testline2 TE 1ST"),
          ("Lived at current address for more than 3 years", "No"),
          ("Previous address", "Testline11 Testline22 TE1 1ST"),
          ("Business email address", "test@foo.com"),
          ("Business daytime phone number", "123"),
          ("Business mobile number", "987654"),
          ("Business website address", "/test/url"),
          ("Business address", "Line1 Line2 XX XX United Kingdom"),
          ("Contact preference", Email.toString),
          ("What does the business do?", "test company desc"),
          ("Main business activity", "super business"),
          ("SIC codes", "super business"),
          ("Confirm the business’s Standard Industry Classification (SIC) codes for VAT", "AB123 - super business"),
          ("VAT start date", "The date the company is registered with Companies House"),
          ("Zero rated turnover for the next 12 months", "£1,234.00"),
          ("Expect to claim VAT refunds regularly?", "Yes"),
          ("When do you want to submit VAT Returns?", "January, April, July and October"),
          ("Different Trading name", "No"),
          ("Apply for EORI?", "No"),
          ("Have a business bank account set up", "Yes"),
          ("Bank details", "testName 12345678 12-34-56"),
          ("Do you want to join the Flat Rate Scheme?", "No")
        )

        cyaRows.zipWithIndex.foreach { case (tuple, index) => tuple mustBe expectedCyaRows(index) }
      }
    }

    "display the summary page correctly for a NETP" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.contains(fullNetpVatScheme)
        .s4lContainer[SicAndCompliance].cleared
        .vatRegistration.storesNrsPayload(testRegId)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("eligibility-data", fullEligibilityDataJson)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/check-confirm-answers").get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.title() mustBe "Check your answers before sending your application - Register for VAT - GOV.UK"
        document.select("h1").first().text() mustBe "Check your answers before sending your application"

        val cyaRows = document
          .getElementsByTag("dl").last()
          .getElementsByTag("div")
          .asScala.toList.map { element =>
          (
            element.getElementsByTag("dt").first().text(),
            element.getElementsByTag("dd").first().text()
          )
        }

        val expectedCyaRows: List[(String, String)] = List(
          ("Unique Taxpayer Reference number", testSautr),
          ("First Name", testFirstName),
          ("Last Name", testLastName),
          ("Temporary Reference number", testTrn),
          ("Date of birth", "1 January 2020"),
          ("Role in the business", Director.toString),
          ("Former name", "New Name Cosmo"),
          ("Date former name changed", "12 July 2000"),
          ("Email address", "test@t.test"),
          ("Telephone number", testApplicantPhone),
          ("Home address", "Testline1 Testline2 TE 1ST"),
          ("Lived at current address for more than 3 years", "No"),
          ("Previous address", "Testline11 Testline22 TE1 1ST"),
          ("Business email address", "test@foo.com"),
          ("Business daytime phone number", "123"),
          ("Business mobile number", "987654"),
          ("Business website address", "/test/url"),
          ("Business address", "Line1 Line2 XX XX United Kingdom"),
          ("Contact preference", Email.toString),
          ("What does the business do?", "test company desc"),
          ("Main business activity", "super business"),
          ("SIC codes", "super business"),
          ("Confirm the business’s Standard Industry Classification (SIC) codes for VAT", "AB123 - super business"),
          ("Zero rated turnover for the next 12 months", "£1,234.00"),
          ("Expect to claim VAT refunds regularly?", "Yes"),
          ("When do you want to submit VAT Returns?", "January, April, July and October"),
          ("Will the business send goods directly to customers from overseas countries?", "Yes"),
          ("Do you intend to send goods direct to customers from within the EU?", "Yes"),
          ("Where are you storing your goods for dispatch?", "Within the United Kingdom"),
          ("Will the business dispatch goods from a FHDDS registered warehouse?", "Yes"),
          ("Fulfilment Warehouse number", testWarehouseNumber),
          ("Fulfilment Warehouse business name", testWarehouseName),
          ("Trading name", testCompanyName),
          ("Have a business bank account set up", "Yes"),
          ("Bank details", "testName BIC IBAN"),
          ("Do you want to join the Flat Rate Scheme?", "No")
        )

        cyaRows.zipWithIndex.foreach { case (tuple, index) => tuple mustBe expectedCyaRows(index) }
      }
    }
  }

  "POST Summary Page" should {
    "redirect to the confirmation page" when {
      "the user is in draft with a vat ready submission" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.contains(vatReg)
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatRegistration.status(s"/vatreg/${vatReg.id}/status", "draft")
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration")

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show().url)
        }
      }
    }
  }
}