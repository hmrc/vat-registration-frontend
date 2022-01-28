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

import forms.OverBusinessGoodsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.annual_costs_inclusive

class AnnualCostsInclusiveViewSpec extends VatRegViewSpec {

  val view: annual_costs_inclusive = app.injector.instanceOf[annual_costs_inclusive]
  val subheading = "VAT registration"
  val heading = "Will the business spend more than £250 over the next 3 months on ‘relevant goods’?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1 = "‘Relevant goods’ are moveable items or materials used exclusively to run the company. They include gas and electricity."
  val listHead = "Do not include:"
  val bullet1 = "travel and accommodation expenses"
  val bullet2 = "food and drink consumed by directors or staff"
  val bullet3 = "vehicle costs including fuel, unless the company is in the transport business and staff are using their own or a leased vehicle"
  val bullet4 = "rent, internet, phone bill"
  val bullet5 = "accountancy fees"
  val bullet6 = "gifts, promotional items and donations"
  val bullet7 = "goods the company will resell or hire out unless this is its main business activity"
  val bullet8 = "training and memberships"
  val bullet9 = "capital items, for example office equipment, laptops, mobile phones and tablets"
  val bullet10 = "services - that is, anything not classified as ‘goods’"
  val para2 = "If you cannot decide right now, answer ‘no’. The business can register for the Flat Rate Scheme at a later date."
  val link = "VAT Flat Rate Scheme (opens in new tab)"
  val para3 = s"Find out more about $link."
  val continue = "Save and continue"

  implicit val doc: Document = Jsoup.parse(view(OverBusinessGoodsForm.form).body)

  "The Relevant Goods Spend page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct subheading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(subheading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have the correct h2" in new ViewSetup {
      doc.headingLevel2(2) mustBe Some(listHead)
    }

    "have correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para1)
    }

    "have correct bullets" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        bullet1,
        bullet2,
        bullet3,
        bullet4,
        bullet5,
        bullet6,
        bullet7,
        bullet8,
        bullet9,
        bullet10
      )
    }
    "have a final paragraph with a help link" in new ViewSetup {
      doc.para(2) mustBe Some(para2)
      doc.para(3) mustBe Some(para3)
      doc.link(1) mustBe Some(Link(link, "https://www.gov.uk/vat-flat-rate-scheme"))
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}
