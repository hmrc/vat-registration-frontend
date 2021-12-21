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

package views

import forms.SoleTraderNameForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.soletrader_name

class SoleTraderNameViewSpec extends VatRegViewSpec {
  val view: soletrader_name = app.injector.instanceOf[soletrader_name]

  object ExpectedContent {
    val heading  = "What is your trading name?"
    val title    = s"$heading - Register for VAT - GOV.UK"
    val summary  = "What is a trading name?"
    val content  = "A sole trader can trade using a name that’s different from their registered name. This is also known as a ‘business name’. " +
                   "Some sole traders choose a different trading name to help with branding or getting a domain name for their website."
    val label    = "Enter the trading name"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(SoleTraderNameForm.form).body)

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
      doc.textBox("trading-name") mustBe Some(ExpectedContent.label)
    }

    "have the correct details" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.summary, ExpectedContent.content))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
