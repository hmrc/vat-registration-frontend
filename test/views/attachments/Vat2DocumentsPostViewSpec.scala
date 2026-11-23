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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.Vat2DocumentsPost

class Vat2DocumentsPostViewSpec extends VatRegViewSpec {

  val view: Vat2DocumentsPost = app.injector.instanceOf[Vat2DocumentsPost]

  val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {

    val title = "Posting your document"

    val para1 =
      "After you have submitted your registration application online, you need to post us:"

    val vat2 = "VAT2"
    val vat2Text = "- partnership details"

    val proofOfIdentity = "Your proof of identity"
    val identityEvidence = "Your identity evidence"

    val insetText: String =
      "You also need to send the auto-generated HMRC cover letter with your documents. " +
        "You can only print this off after you have submitted your online VAT application."

    val postalAddress =
      "VAT Registration Applications BT VAT HM Revenue and Customs BX9 1WR United Kingdom"
  }

  "Vat2DocumentsPost view" should {

    "have a back link" in {
      doc.select(".govuk-back-link").size() mustBe 1
    }

    "have the correct page title" in {
      doc.title must include(ExpectedContent.title)
    }

    "display the introductory paragraph" in {
      doc.text() must include(ExpectedContent.para1)
    }

    "display the inset text" in {
      doc.select(".govuk-inset-text").text() mustBe ExpectedContent.insetText
    }

    "display the VAT2 form information" in {
      doc.text() must include(ExpectedContent.vat2)
      doc.text() must include(ExpectedContent.vat2Text)
    }

    "display the identity document information" in {
      doc.text() must include(ExpectedContent.proofOfIdentity)
      doc.text() must include(ExpectedContent.identityEvidence)
    }

    "display the identity documents details" in {
      doc.text() must include("your passport")
      doc.text() must include("your photocard driving licence")
      doc.text() must include("your national identity card")
      doc.text() must include("We also need copies of any 2 of the following:")
    }

    "display the postal address" in {
      doc.text() must include(ExpectedContent.postalAddress)
    }

    "display the Continue button" in {
      doc.select(".govuk-button").text() mustBe "Continue"
    }
  }
}