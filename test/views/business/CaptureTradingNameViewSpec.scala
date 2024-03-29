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

package views.business

import forms.CaptureTradingNameForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.business.CaptureTradingNameView

class CaptureTradingNameViewSpec extends VatRegViewSpec {
  val view: CaptureTradingNameView = app.injector.instanceOf[CaptureTradingNameView]

  object ExpectedContent {
    val heading = "What is your trading name?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val summary = "What is a trading name?"
    val content = "A business can trade using a name that’s different from their registered name. This is also known as a ‘trading name’. " +
      "Some businesses choose a different trading name to help with branding or getting a domain name for their website."
    val label = "Enter the trading name"
    val hint = "You cannot include ‘limited’, ‘Ltd’, ‘limited liability partnership’, ‘LLP’, ‘public limited company’ or ‘plc’"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(CaptureTradingNameForm.form).body)

  "Sole Trader Name page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct label" in new ViewSetup {
      doc.textBox("captureTradingName") mustBe Some(ExpectedContent.label)
    }

    "have the correct details" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.summary, ExpectedContent.content))
    }

    "have the correct hint" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedContent.hint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
