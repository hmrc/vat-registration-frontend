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
import views.html.attachments.Vat5LDocumentsPost

class Vat5LDocumentsPostViewSpec extends VatRegViewSpec {

  val view: Vat5LDocumentsPost =
    app.injector.instanceOf[Vat5LDocumentsPost]

  val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val title = "Posting your document"

    val para1 =
      "After you have submitted your registration application online, you need to post us:"

    val vat5L = "VAT5L form"
    val vat5LText = "completed"

    val proofOfIdentity = "a copy of your proof of identity"
    val identityEvidence = "copies of your identity evidence"

    val landPara2 = "You may also need to send a completed:"
    val vat1614A = "VAT1614A"
    val vat1614AText =
      "- to notify HMRC of your decision to opt to tax land or buildings"

    val vat1614H = "VAT1614H"
    val vat1614HText =
      "- if you want to opt to tax land or buildings and need HMRC permission"

    val supportingHeading = "Supporting documents"

    val supportingPara: String =
      "You can also send any supporting documents for your VAT5L. " +
        "Supporting documents are optional. However, sending them now may save delays to your application if we decide we need to ask for them later."

    val supportingLink =
      "Find out more about supporting documents for your VAT5L"

    val insetText: String =
      "You also need to send the auto-generated HMRC cover letter with your documents. " +
        "You can only print this off after you have submitted your online VAT application."

    val postalAddress =
      "VAT Registration Applications BT VAT HM Revenue and Customs BX9 1WR United Kingdom"
  }

  "Vat5LDocumentsPost view" should {

    "display the correct page heading" in {
      doc.select("h1").text() mustBe ExpectedContent.title
    }

    "display the correct page title" in {
      doc.title must include(ExpectedContent.title)
    }

    "display the introductory paragraph" in {
      doc.text() must include(ExpectedContent.para1)
    }

    "display the VAT5L form information" in {
      doc.text() must include(ExpectedContent.vat5L)
      doc.text() must include(ExpectedContent.vat5LText)
    }

    "display the identity document information" in {
      doc.text() must include(ExpectedContent.proofOfIdentity)
      doc.text() must include(ExpectedContent.identityEvidence)
      doc.text() must include("your passport")
      doc.text() must include("your photocard driving licence")
      doc.text() must include("your national identity card")
    }

    "display the land and property information" in {
      doc.text() must include(ExpectedContent.landPara2)
      doc.text() must include(ExpectedContent.vat1614A)
      doc.text() must include(ExpectedContent.vat1614H)
    }

    "display the supporting documents heading" in {
      doc.text() must include(ExpectedContent.supportingHeading)
    }

    "display the supporting documents paragraph" in {
      doc.text() must include(ExpectedContent.supportingPara)
    }

    "display the VAT5L guidance link" in {
      doc.select("a[href]").eachText().toString must include(
        ExpectedContent.supportingLink
      )
    }

    "display the inset text" in {
      doc.select(".govuk-inset-text").text() mustBe ExpectedContent.insetText
    }

    "display the postal address" in {
      doc.text() must include(ExpectedContent.postalAddress)
    }

    "display the Continue button" in {
      doc.select(".govuk-button").text() mustBe "Continue"
    }
  }
}
