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

import features.returns.{Frequency, Returns, Stagger}
import it.fixtures.VatRegistrationFixture
import models.S4LVatSicAndCompliance
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class SummaryControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with VatRegistrationFixture {
  "GET Summary page" should {
    "display the summary page correctly" when {
      "the company is NOT incorporated" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .vatScheme.contains(vatReg)
          .s4lContainer[S4LVatSicAndCompliance].cleared
          .s4lContainer[Returns].contains(Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None))
          .audit.writesAudit()

        val response = buildClient("/check-your-answers").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          document.title() mustBe "Summary"
          
          a[NullPointerException] mustBe thrownBy(document.getElementById("threshold.overThresholdSelectionQuestion").text)
          document.getElementById("vatDetails.taxableTurnoverAnswer").text mustBe "No"
          document.getElementById("vatDetails.taxableTurnoverChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/vat-taxable-sales-over-threshold"
          document.getElementById("vatDetails.necessityAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.necessityChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/register-voluntarily"
          document.getElementById("vatDetails.voluntaryRegistrationReasonAnswer").text mustBe "The company is already selling goods or services"
          document.getElementById("vatDetails.voluntaryRegistrationReasonChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/applies-company"

          document.getElementById("directorDetails.formerNameAnswer").text mustBe "No former name"
          document.getElementById("taxableSales.estimatedSalesValueAnswer").text mustBe "£30000"
          document.getElementById("annualAccountingScheme.accountingPeriodAnswer").text mustBe "January, April, July and October"
        }
      }

      "the company is incorporated" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .vatScheme.contains(vatRegIncorporated)
          .s4lContainer[S4LVatSicAndCompliance].cleared
          .audit.writesAudit()

        val response = buildClient("/check-your-answers").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)

          a[NullPointerException] mustBe thrownBy(document.getElementById("vatDetails.taxableTurnoverQuestion").text)
          document.getElementById("vatDetails.overThresholdSelectionQuestion").text must include("05 August 2016")
          document.getElementById("vatDetails.overThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.overThresholdSelectionChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/vat-taxable-turnover-gone-over"
          document.getElementById("vatDetails.overThresholdDateAnswer").text mustBe "September 2016"
          document.getElementById("vatDetails.overThresholdDateChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/vat-taxable-turnover-gone-over"
          document.getElementById("vatDetails.expectedOverThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.expectedOverThresholdSelectionChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/go-over-vat-threshold-period"
          document.getElementById("vatDetails.expectedOverThresholdDateAnswer").text mustBe "30 September 2016"
          document.getElementById("vatDetails.expectedOverThresholdDateChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/go-over-vat-threshold-period"
        }
      }
    }
  }
}
