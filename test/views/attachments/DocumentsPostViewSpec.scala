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
import views.html.attachments.DocumentsPost

class DocumentsPostViewSpec extends VatRegViewSpec {

  val view: DocumentsPost = app.injector.instanceOf[DocumentsPost]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "Posting your documents and forms"
    val title = s"$heading - Register for VAT - GOV.UK"

    val para1 =
      "You need to post your documents and forms after you have submitted your VAT registration application online."

    val formsPara =
      "Make sure you have all your forms and copies of your identity documents (links open in a new tab):"

    val vat2 = "VAT2 - partnership details"
    val vat5L = "VAT5L - land and property supplies (you can also include any supporting documents)"
    val vat5051 = "VAT 50/51 - VAT group registration"
    val vat1TR = "VAT1TR - a non-established taxable person (NETP) looking to appoint a tax representative"

    val proofOfIdentity = "Proof of identity"
    val identityEvidence = "Identity evidence"

    val identityHeading = "What identity documents can I provide?"
    val proofHeading = "Proof of identity"
    val otherDocumentsHeading = "Other identity documents"

    val identityPara = "We need a copy of one of the following:"
    val otherDocumentsPara = "We also need copies of any 2 of the following:"

    val identityDocuments: Seq[String] = Seq(
      "your passport",
      "your photocard driving licence",
      "your national identity card",
      "your birth certificate",
      "a lease or rental agreement",
      "your work permit or visa",
      "a recent utility bill",
      "a recent mortgage statement",
      "a recent bank statement",
      "a recent credit card statement",
      "any documents from the Department for Work and Pensions that confirms your entitlement to benefits"
    )

    val identityPara3 = "Your documents should be dated within the last 3 months, where possible."

    val identityWarning =
      "Do not send original versions of your documents. You only need to send us copies."

    val landHeading = "If you want to charge VAT on land or buildings"
    val landPara = "You may also need to send a:"
    val vat1614A = "VAT1614A - to notify HMRC of your decision to opt to tax land and or buildings"
    val vat1614H = "VAT1614H - if you want to opt to tax land and or buildings and need HMRC permission"

    val supportingDocumentsHeading = "Supporting documents"

    val coverHeading = "Cover page"
    val coverText =
      "You need to include the cover page with your documents and forms. You'll be able to print the page after you submit your application online."

    val postalHeading = "Postal address"
    val address =
      "VAT Registration Applications BT VAT HM Revenue and Customs BX9 1WR United Kingdom"

    val continue = "Continue"
  }

  "Documents post page" must {

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
      doc.title mustBe ExpectedContent.title
    }

    "have the correct introductory text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
    }

    "have the forms heading and introductory text" in new ViewSetup {
      doc.select("h2").text() must include("Forms and identity documents")
      doc.text() must include(ExpectedContent.formsPara)
    }

    "display all required forms and identity bullets" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.vat2)
      pageText must include(ExpectedContent.vat5L)
      pageText must include(ExpectedContent.vat5051)
      pageText must include(ExpectedContent.vat1TR)
      pageText must include(ExpectedContent.proofOfIdentity)
      pageText must include(ExpectedContent.identityEvidence)
    }

    "display the identity documents details section" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.identityHeading)
      pageText must include(ExpectedContent.proofHeading)
      pageText must include(ExpectedContent.otherDocumentsHeading)
      pageText must include(ExpectedContent.identityPara)
      pageText must include(ExpectedContent.otherDocumentsPara)

      ExpectedContent.identityDocuments.foreach { document =>
        pageText must include(document)
      }

      pageText must include(ExpectedContent.identityPara3)
      pageText must include(ExpectedContent.identityWarning)
    }

    "display the land and buildings section" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.landHeading)
      pageText must include(ExpectedContent.landPara)
      pageText must include(ExpectedContent.vat1614A)
      pageText must include(ExpectedContent.vat1614H)
    }

    "display the supporting documents section" in new ViewSetup {
      doc.text() must include(ExpectedContent.supportingDocumentsHeading)
      doc.text() must include("supporting documents for your VAT5L")
    }

    "display the cover page information" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.coverHeading)
      pageText must include(ExpectedContent.coverText)
    }

    "display the postal address" in new ViewSetup {
      doc.text() must include(ExpectedContent.address)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}