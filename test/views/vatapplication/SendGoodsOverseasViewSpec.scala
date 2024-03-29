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

package views.vatapplication

import org.jsoup.Jsoup
import play.api.data.Form
import play.api.data.Forms.{boolean, single}
import views.VatRegViewSpec
import views.html.vatapplication.SendGoodsOverseasView


class SendGoodsOverseasViewSpec extends VatRegViewSpec {
  val form = Form(single("value" -> boolean))
  val view = app.injector.instanceOf[SendGoodsOverseasView]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Will the business send goods directly to customers from overseas countries?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val hint = "England, Scotland, Wales and Northern Ireland are the only countries not considered as overseas for the purposes of VAT."
    val error = "Select yes if the business send goods directly to customers from overseas countries"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "The Send Goods Overseas page" must {
    "have correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have correct hint" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe ExpectedContent.hint
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
