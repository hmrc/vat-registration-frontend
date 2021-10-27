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

package views

import forms.TradingNameForm
import org.jsoup.Jsoup
import views.html.trading_name

class TradingNameViewSpec extends VatRegViewSpec {

  val testCompanyName = "testCompanyName"
  val form = TradingNameForm.form
  val view = app.injector.instanceOf[trading_name]
  implicit val doc = Jsoup.parse(view(form, testCompanyName).body)

  object ExpectedContent {
    val heading = s"Does or will the business trade using a name that is different from $testCompanyName?"
    val detailsSummary = "What is a trading name?"
    val detailsContent = "A business can trade using a name that’s different from its official or registered name. " +
      "This is also known as a ’trading name’. " +
      "Some businesses choose a different trading name to help with branding or getting a domain name for their website."
    val hint = "You cannot include ’limited’, ’Ltd’, ’limited liability partnership’, ’LLP’, ’public limited company’ or ’plc’"
    val label = "Enter the trading name"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "The Trading name view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a progressive disclosure" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.detailsSummary, ExpectedContent.detailsContent))
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a textbox with the correct label" in new ViewSetup {
      doc.textBox("tradingName") mustBe Some(ExpectedContent.label)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
