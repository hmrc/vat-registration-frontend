/*
 * Copyright 2023 HM Revenue & Customs
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

package views.vatapplication

import forms.PaymentMethodForm
import models.api.vatapplication.PaymentMethod
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.VatRegViewSpec
import views.html.vatapplication.AasPaymentMethod

class PaymentMethodViewSpec extends VatRegViewSpec {

  object ExpectedMessages {
    val title = "How does the business want to pay VAT?"
    val heading = "How does the business want to pay VAT?"
    val paragraph = "HMRC will contact the business if payment method or bank details are changed during the accounting year. This is to protect the VAT account from fraud."
    val bacs = "BACS (internet banking) or Direct Debit"
    val giro = "Bank Giro Transfer"
    val chaps = "Clearing House Automated Payment System (CHAPS)"
    val standingOrder = "Standing order"
    val saveAndContinue = "Save and Continue"
    val saveAndComeBackLater = "Save and come back later"
    val error = "Select the VAT payment method"
    val linkText = "print off and fill in a Direct debit mandate (opens in new tab)"
    val hiddenText = s"To pay by Direct Debit, complete this VAT registration to get the VAT Registration Number. The business can then $linkText"
  }

  val view = app.injector.instanceOf[AasPaymentMethod]

  def asDocument(form: Form[PaymentMethod]): Document = Jsoup.parse(view(form).body)

  "the AAS Payment Method view" must {
    "have the right page title" in new ViewSetup()(asDocument(PaymentMethodForm())) {
      doc.title must include(ExpectedMessages.title)
    }
    "have the right heading" in new ViewSetup()(asDocument(PaymentMethodForm())) {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct paragraph" in new ViewSetup()(asDocument(PaymentMethodForm())) {
      doc.select(Selectors.p).get(0).text() mustBe ExpectedMessages.paragraph
    }

    "have the correct hiddenBACS content" in new ViewSetup()(asDocument(PaymentMethodForm())) {
      doc.select(Selectors.p).get(1).text() mustBe ExpectedMessages.hiddenText
    }

    "have the correct content for each option" in new ViewSetup()(asDocument(PaymentMethodForm())) {
      val validOptions = Map(
        "bacs" -> ExpectedMessages.bacs,
        "giro" -> ExpectedMessages.giro,
        "chaps" -> ExpectedMessages.chaps,
        "standing-order" -> ExpectedMessages.standingOrder
      )

      validOptions map {
        case (value, expectedContent) =>
          doc.radio(value) mustBe Some(expectedContent)
      }
    }
    "show the correct error message if a value isn't selected"in new ViewSetup()(
      doc = asDocument(PaymentMethodForm().bind(Map("value" -> "")))
    ) {
      doc.hasErrorSummary mustBe true
      doc.errorSummaryLinks must contain(Link(ExpectedMessages.error, "#value"))
    }
  }

}