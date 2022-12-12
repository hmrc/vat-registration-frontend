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

package views.vatapplication

import forms.ApplyForEoriForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.VatRegViewSpec
import views.html.vatapplication.OverseasApplyForEori

class OverseasApplyForEoriViewSpec extends VatRegViewSpec {

  val view: OverseasApplyForEori = app.injector.instanceOf[OverseasApplyForEori]
  implicit val doc: Document = Jsoup.parse(view(ApplyForEoriForm.form).body)

  object ExpectedContent {
    val p1 = "If your business is not based in the country you’re moving goods to or from, you should get an EORI number if you’re:"

    val bullet1 = "making a customs declaration - check if you’re eligible to make a customs declaration"
    val bullet2 = "making an entry summary declaration"
    val bullet3 = "making an exit summary declaration"
    val bullet4 = "making a temporary storage declaration"
    val bullet5 = "making a customs declaration for temporary admission or re-export declaration where you have a guarantee"
    val bullet6 = "acting as a carrier for transporting goods by sea, inland waterway or air"
    val bullet7 = "acting as a carrier connected to the customs system and you want to get notifications regarding the lodging or amendment of entry summary declarations"
    val bullet8 = "established in a common transit country where the declaration is lodged instead of an entry summary declaration or is used as a pre-departure declaration"

    val p2 = "If you’re not eligible to apply for an EORI number yourself, you’ll need to appoint someone to deal with customs on your behalf. The person you appoint will need to get the EORI number instead of you."
    val p3 = "If you’re based in the Channel Islands and you move goods to or from the UK, you do not need an EORI number. You’ll need an EORI number if you use HMRC’s customs systems like Customs Handling of Import and Export Freight (CHIEF)."

    val link1Text = "check if you’re eligible to make a customs declaration"
    val link2Text = "entry summary declaration"
    val link3Text = "exit summary declaration"
    val link4Text = "temporary storage declaration"
    val link5Text = "customs declaration for temporary admission or re-export declaration where you have a guarantee"
    val link6Text = "common transit country"
    val link7Text = "appoint someone to deal with customs on your behalf."
    val infoLinkText = "Find out more about EORI numbers (opens in new tab)"

    val title = "Do you need an EORI number? - Register for VAT - GOV.UK"
    val heading = "Do you need an EORI number?"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "overseas Apply For Eori page" must {
    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
    }

    "have the correct bullets" in new ViewSetup {
      doc.unorderedList(1) mustBe
        List(
          ExpectedContent.bullet1,
          ExpectedContent.bullet2,
          ExpectedContent.bullet3,
          ExpectedContent.bullet4,
          ExpectedContent.bullet5,
          ExpectedContent.bullet6,
          ExpectedContent.bullet7,
          ExpectedContent.bullet8
        )
    }

    "have the correct inset section" in new ViewSetup {
      val sections: Elements = doc.select(Selectors.indent).select("p.govuk-body")
      sections.get(0).text mustBe ExpectedContent.p2
      sections.get(1).text mustBe ExpectedContent.p3
    }

    "have the correct link texts" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link1Text, "https://www.gov.uk/check-customs-declaration/"))
      doc.link(2) mustBe Some(Link(ExpectedContent.link2Text, "https://www.gov.uk/guidance/making-an-entry-summary-declaration"))
      doc.link(3) mustBe Some(Link(ExpectedContent.link3Text, "https://www.gov.uk/guidance/find-out-when-to-make-an-exit-summary-declaration"))
      doc.link(4) mustBe Some(Link(ExpectedContent.link4Text, "https://www.gov.uk/guidance/how-to-put-goods-into-a-temporary-storage-facility"))
      doc.link(5) mustBe Some(Link(ExpectedContent.link5Text, "https://www.gov.uk/guidance/apply-to-import-goods-temporarily-to-the-uk-or-eu"))
      doc.link(6) mustBe Some(Link(ExpectedContent.link6Text, "https://www.gov.uk/guidance/common-transit-convention-countries"))
      doc.link(7) mustBe Some(Link(ExpectedContent.link7Text, "https://www.gov.uk/guidance/appoint-someone-to-deal-with-customs-on-your-behalf"))
      doc.link(8) mustBe Some(Link(ExpectedContent.infoLinkText, "https://www.gov.uk/eori"))
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
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
