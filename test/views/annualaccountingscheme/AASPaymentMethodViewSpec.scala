/*
 * Copyright 2021 HM Revenue & Customs
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

package views.annualaccountingscheme

import forms.AASPaymentMethodForm
import models.AASPaymentMethod
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.libs.json.Json
import views.VatRegViewSpec
import views.html.annualaccountingscheme.aas_payment_method

class AASPaymentMethodViewSpec extends VatRegViewSpec {

  object ExpectedMessages {
    val title = "How do you want to pay VAT?"
    val heading = "How do you want to pay VAT?"
    val insetText = "If you would prefer to pay by Direct Debit you can set this up after you have received your VAT Registration Number."
    val bacs = "BACS or internet banking"
    val giro = "Bank Giro Transfer"
    val chaps = "Clearing House Automated Payment System (CHAPS)"
    val standingOrder = "Standing order"
    val saveAndContinue = "Save and Continue"
    val saveAndComeBackLater = "Save and come back later"
    val error = "Tell us how you want to pay VAT bills"
  }

  val view = app.injector.instanceOf[aas_payment_method]

  def asDocument(form: Form[AASPaymentMethod]): Document = Jsoup.parse(view(form).body)

  "the AAS Payment Method view" must {
    "have the right page title" in new ViewSetup()(asDocument(AASPaymentMethodForm())) {
      doc.title must include(ExpectedMessages.title)
    }
    "have the right heading" in new ViewSetup()(asDocument(AASPaymentMethodForm())) {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct inset text" in new ViewSetup()(asDocument(AASPaymentMethodForm())) {
      doc.select(Selectors.indent).text mustBe ExpectedMessages.insetText
    }
    "have the correct content for each option" in new ViewSetup()(asDocument(AASPaymentMethodForm())) {
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
      doc = asDocument(AASPaymentMethodForm().bind(Json.obj("value" -> "")))
    ) {
      doc.hasErrorSummary mustBe true
      doc.errorSummaryLinks must contain(Link(ExpectedMessages.error, "#value"))
    }
  }

}