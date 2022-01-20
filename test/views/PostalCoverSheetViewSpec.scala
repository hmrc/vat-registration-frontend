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

import models.api.{AttachmentType, IdentityEvidence, VAT2}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.PostalCoverSheet

class PostalCoverSheetViewSpec extends VatRegViewSpec {

  val postalCoverSheetPage: PostalCoverSheet = app.injector.instanceOf[PostalCoverSheet]

  val testRef = "VRN12345689"
  val testAttachments = List[AttachmentType](VAT2, IdentityEvidence)
  val testVat2 = List[AttachmentType](VAT2)

  lazy val view: Html = postalCoverSheetPage(ackRef = testRef, attachments = testAttachments)
  implicit val doc: Document = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "Print cover letter for documents"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "Print this page and include it with the documents you are sending to HMRC. This will enable us to match your online application to your supporting documents."
    val warningText = "Do not post the original documents to HMRC as we are unable to return them to you."
    val panel1 = s"Register for VAT reference number: $testRef"
    val heading2 = "What you must post to us"
    val para2 = "You must send us additional documents in order for us to process this VAT application:"
    val para3 = "Include this cover letter."
    val heading3 = "Postal address"
    val para4 = "Send the supporting documents and covering letter to:"
    val panel2 = "VAT Registration Applications BT VAT HM Revenue and Customs BX9 1WR United Kingdom"
    val vat2Bullet = "a completed VAT2 form (opens in new tab) to capture the details of all the partners"
    val idEvidence = "three documents to confirm your identity"
    val print = "Print this page"
  }

  object IdentityEvidenceBlock {
    val summary = "What identity documents can I provide?"
    val content = "Include a copy of one piece of evidence that includes a government issued photo. This could be a: " +
    "passport " +
    "driving licence photocard " +
    "national identity card " +
    "And " +
    "Also include two additional pieces of evidence which can be copies of a: " +
    "mortgage statement " +
    "lease or rental agreement " +
    "work permit or visa " +
    "letter from the Department for Work and Pensions for confirming entitlement to benefits " +
    "utility bill " +
    "birth certificate"
  }


  "The Postal Cover Sheet page" must {
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct warning text" in new ViewSetup {
      doc.warningText(1) match {
        case Some(value) => value must include(ExpectedContent.warningText)
        case None => fail()
      }
    }

    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).get(0).text mustBe ExpectedContent.panel1
    }

    "have the correct heading2" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.heading2)
    }
    "have the correct heading3" in new ViewSetup {
      doc.headingLevel2(2) mustBe Some(ExpectedContent.heading3)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct paragraph1 text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
    }

    "have the correct paragraph2 text" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the correct paragraph3 text" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.para3)
    }

    "not show the identity documents dropdown when attachment list does not contain IdentityEvidence" in new ViewSetup {
      val view: Html = postalCoverSheetPage(ackRef = testRef, attachments = testVat2)
      override val doc: Document = Jsoup.parse(view.body)
      doc.unorderedList(1) mustBe List(ExpectedContent.vat2Bullet)
      doc.unorderedList(1) mustNot contain(ExpectedContent.idEvidence)
    }

    "have the correct first bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.vat2Bullet,
        ExpectedContent.idEvidence
      )
    }

    "have a details block" in new ViewSetup {
      doc.details mustBe Some(Details(IdentityEvidenceBlock.summary, IdentityEvidenceBlock.content))
    }

    "have the correct panel text two" in new ViewSetup {
      doc.select(Selectors.indent).get(1).text mustBe ExpectedContent.panel2
    }

    "have a print button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.print)
    }
  }
}

