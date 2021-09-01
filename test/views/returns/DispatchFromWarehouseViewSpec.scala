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
import views.html.returns.DispatchFromWarehouseView

class DispatchFromWarehouseViewSpec extends VatRegViewSpec {

  val form = Form(single("value" -> boolean))
  val view = app.injector.instanceOf[DispatchFromWarehouseView]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Will the business dispatch goods from a Fulfilment House Due Diligance Scheme (FHDDS) registered warehouse?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val linkText = "searching the registered businesses list (opens in new tab)"
    val url = "https://www.gov.uk/government/publications/fulfilment-house-due-diligence-scheme-registered-businesses-list"
    val para1 = s"You can check if the business that stores and dispatches your goods in the UK is registered with the FHDDS by $linkText."
    val error = "Select yes if you expect the business to regularly claim VAT refunds from HMRC"
    val continue = "Save and continue"
    val yes = "Yes"
    val no = "No"
  }

  "The charge expectancy (regularly claim refunds) page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, ExpectedContent.url))
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
