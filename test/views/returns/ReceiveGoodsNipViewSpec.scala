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

import forms.ReceiveGoodsNipForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.returns.ReceiveGoodsNip

class ReceiveGoodsNipViewSpec extends VatRegViewSpec {

  val form = ReceiveGoodsNipForm.form
  val view = app.injector.instanceOf[ReceiveGoodsNip]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Does the business expect to receive goods in Northern Ireland from an EU country?"
    val hint = "What is the value of these goods?"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "The ReceiveGoodsNIP name view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a box with the correct label" in new ViewSetup {
      doc.textBox("northernIrelandReceiveGoods") mustBe Some(ExpectedContent.hint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}