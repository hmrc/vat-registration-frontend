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

package views.returns

import org.jsoup.Jsoup
import play.api.data.Form
import play.api.data.Forms.{boolean, single}
import views.VatRegViewSpec
import views.html.returns.SendEUGoodsView

class SendEUGoodsViewSpec extends VatRegViewSpec {

  val form = Form(single("value" -> boolean))
  val view = app.injector.instanceOf[SendEUGoodsView]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Do you intend to send goods direct to customers from within the EU?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val error = "Select yes if the business intends to send goods direct to customers from within the EU."
    val continue = "Save and continue"
    val yes = "Yes"
    val no = "No"
  }

  "The Send EU Goods page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
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
