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

package views.sicandcompliance

import forms.LandAndPropertyForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.sicandcompliance.LandAndProperty

class LandAndPropertyViewSpec extends VatRegViewSpec {

  val view: LandAndProperty = app.injector.instanceOf[LandAndProperty]
  implicit val doc: Document = Jsoup.parse(view(LandAndPropertyForm.form).body)

  object ExpectedContent {
    val link1 = "read the guidance on land and property (opens in new tab)"
    val p1 = s"Some transactions involving land building are exempt from VAT. You can $link1."
    val heading = "Does the business’s activities include the buying, selling or letting of land or property?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "the Land And Property page" must {
    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link1, "https://www.gov.uk/guidance/vat-on-land-and-property-notice-742"))
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
