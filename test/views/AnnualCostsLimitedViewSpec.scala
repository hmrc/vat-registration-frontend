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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import forms.OverBusinessGoodsPercentForm
import views.html.annual_costs_limited

class AnnualCostsLimitedViewSpec extends VatRegViewSpec with OverBusinessGoodsPercentForm {

  val view: annual_costs_limited = app.injector.instanceOf[annual_costs_limited]

  val pct: Long = 1
  val heading = "Will the business spend more than £1, including VAT, on relevant goods over the next 3 months?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val continue = "Save and continue"

  implicit val doc: Document = Jsoup.parse(view(form, pct).body)

  "The Join FRS page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}
