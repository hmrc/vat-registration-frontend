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

package views.partners

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.partners.AdditionalPartnersRequired

class AdditionalPartnersRequiredViewSpec extends VatRegViewSpec {

  val view: AdditionalPartnersRequired = app.injector.instanceOf[AdditionalPartnersRequired]
  implicit val doc: Document = Jsoup.parse(view().body)

  val title = "To add more partners, you must send us a completed VAT2 form - Register for VAT - GOV.UK"
  val heading = "To add more partners, you must send us a completed VAT2 form"
  val para = "We need a completed VAT2 form (opens in new tab) with details of the partners in the partnership."
  val link = "VAT2 form (opens in new tab)"
  val buttonText = "Continue"

  "Additional partners required page" should {

    "have the correct title" in new ViewSetup {
      doc.title mustBe title
    }

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, appConfig.vat2Link))
    }

    "have a continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
