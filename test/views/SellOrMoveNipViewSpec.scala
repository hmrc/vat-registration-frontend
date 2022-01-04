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

import forms.SellOrMoveNipForm
import org.jsoup.Jsoup
import views.html.returns.SellOrMoveNip

class SellOrMoveNipViewSpec extends VatRegViewSpec {
  val form = SellOrMoveNipForm.form
  val view = app.injector.instanceOf[SellOrMoveNip]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Does the business expect to do any of the following in the next 12 months?"
    val para = "Tell us if the business will do any of the following:"
    val bullet1 = "sell goods located in Northern Ireland to any country"
    val bullet2 = "sell or move goods from Northern Ireland to an EU country"
    val hint = "What is the value of these goods?"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "The SellOrMoveNip view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct para text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2
      )
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a box with the correct label" in new ViewSetup {
      doc.textBox("sellOrMoveNip") mustBe Some(ExpectedContent.hint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
