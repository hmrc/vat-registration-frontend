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

import forms.{ApplyForEoriForm, LandAndPropertyForm}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.{LandAndProperty, apply_for_eori}

class LandAndPropertyViewSpec extends VatRegViewSpec {

  val view: LandAndProperty = app.injector.instanceOf[LandAndProperty]
  implicit val doc: Document = Jsoup.parse(view(LandAndPropertyForm.form).body)

  object ExpectedContent {
    val link1 = "VAT5L form (opens in new tab)"
    val p1 = s"If the business’s activities include the buying, selling or letting of land or property you must inform HMRC about this by completing a $link1."
    val link2 = "read the guidance on land and property (opens in new tab)"
    val p2 = s"Some transactions involving land building are exempt from VAT. You can $link2."
    val heading = "Does the business’s activities include the buying, selling or letting of land or property?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "the Land And Property page" must {
    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
      doc.para(2) mustBe Some(ExpectedContent.p2)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link1, "https://www.gov.uk/government/publications/vat-vat-registration-land-and-property-vat-5l"))
      doc.link(2) mustBe Some(Link(ExpectedContent.link2, "https://www.gov.uk/guidance/vat-on-land-and-property-notice-742"))
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
