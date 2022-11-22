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

package views.business

import forms.ConfirmTradingNameForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.business.ConfirmTradingNameView

class ConfirmTradingNameViewSpec extends VatRegViewSpec {

  val testCompanyName = "testCompanyName"
  val form = ConfirmTradingNameForm.form
  val view = app.injector.instanceOf[ConfirmTradingNameView]
  implicit val doc = Jsoup.parse(view(form, testCompanyName).body)

  object ExpectedContent {
    val heading = "Is this your trading name?"
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

    "play back the companyName" in new ViewSetup {
      doc.para(1) mustBe Some(testCompanyName)
    }

    "have correct descriptive label" in new ViewSetup {
      doc.select("legend").text() mustBe ExpectedContent.heading
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
