/*
 * Copyright 2026 HM Revenue & Customs
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

package views.attachments

import config.FrontendAppConfig
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.Vat2DocumentsRequired

class Vat2DocumentsRequiredViewSpec extends VatRegViewSpec {

  val view: Vat2DocumentsRequired = app.injector.instanceOf[Vat2DocumentsRequired]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "We need documents from you to process your VAT application"
    val title = s"$heading - Register for VAT - GOV.UK"

    val para = "Based on your answers, you need to:"

    val proofOfIdentity = "provide 3 documents to confirm your identity"

    val vat2 =
      "complete and send us a VAT2 form to tell us about your partnership details"

    val vat2Para =
      "Open the VAT2 page (opens in a new tab) for more information about the form, and to download it so it can be completed and printed."

    val identityHeading = "What identity documents can I provide?"

    val continue = "Continue"
  }

  "VAT2 documents required page" must {

    "have the correct heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
      doc.title mustBe ExpectedContent.title
    }

    "have the correct introductory text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "display the required identity and VAT2 information" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.proofOfIdentity)
      pageText must include(ExpectedContent.vat2)
    }

    "display the VAT2 guidance information with the correct link" in new ViewSetup {
      val vat2GuidanceLink = doc.select("a:containsOwn(the VAT2 page (opens in a new tab))")

      vat2GuidanceLink.size() mustBe 1
      vat2GuidanceLink.attr("href") mustBe appConfig.vat2Link
      vat2GuidanceLink.attr("target") mustBe "_blank"

      doc.text() must include(ExpectedContent.vat2Para)
    }

    "display the identity documents details section" in new ViewSetup {
      doc.text() must include(ExpectedContent.identityHeading)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }

    "not display the save progress button" in new ViewSetup {
      doc.text() must not include "Save and come back later"
    }
  }
}
