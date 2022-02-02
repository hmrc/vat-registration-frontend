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

package views.returns

import forms.PaymentFrequencyForm
import models.api.returns.PaymentFrequency
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.libs.json.Json
import views.VatRegViewSpec
import views.html.returns.payment_frequency

class PaymentFrequencyPageSpec extends VatRegViewSpec {

  object ExpectedMessages {
    val title = "How often does the business want to make payments?"
    val heading = "How often does the business want to make payments?"
    val paragraph = "As part of the Annual Accounting Scheme each payment is either 10% of the estimated VAT bill for monthly payers or 25% for quarterly payers. The amount the business will pay is based on previous VAT Returns or if they have not yet submitted a VAT Return it will be estimated."
    val quarterly = "Quarterly"
    val monthly = "Monthly"
    val saveAndContinue = "Save and Continue"
    val saveAndComeBackLater = "Save and come back later"
    val error = "Tell us how often you want to make payments"
  }

  val view: payment_frequency = app.injector.instanceOf[payment_frequency]

  def asDocument(form: Form[PaymentFrequency]): Document = Jsoup.parse(view(form).body)

  "the how_often_pay_aas view" must {
    "have the right page title" in new ViewSetup()(asDocument(PaymentFrequencyForm())) {
      doc.title must include(ExpectedMessages.title)
    }

    "have the right heading" in new ViewSetup()(asDocument(PaymentFrequencyForm())) {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }

    "have the right paragraph" in new ViewSetup()(asDocument(PaymentFrequencyForm())) {
      doc.para(1) mustBe Some(ExpectedMessages.paragraph)
    }

    "have the correct content for each option" in new ViewSetup()(asDocument(PaymentFrequencyForm())) {
      val validOptions = Map(
        "quarterly" -> ExpectedMessages.quarterly,
        "monthly" -> ExpectedMessages.monthly
      )

      validOptions map {
        case (value, expectedContent) =>
          doc.radio(value) mustBe Some(expectedContent)
      }
    }

    "show the correct error message if a value isn't selected" in new ViewSetup()(
      doc = asDocument(PaymentFrequencyForm().bind(Json.obj("value" -> "")))
    ) {
      doc.hasErrorSummary mustBe true
      doc.errorSummaryLinks must contain(Link(ExpectedMessages.error, "#value"))
    }
  }

}
