/*
 * Copyright 2023 HM Revenue & Customs
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

import featureswitch.core.config.{FeatureSwitching, OptionToTax}
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.EmailCoverSheet

class EmailCoverSheetViewSpec extends VatRegViewSpec with FeatureSwitching {

  val testRef = "VRN12345689"
  val testAttachments: List[AttachmentType] = List[AttachmentType](VAT2, VAT51, IdentityEvidence, VAT5L, TaxRepresentativeAuthorisation)
  val testVat2: List[AttachmentType] = List[AttachmentType](VAT2)
  val testVat5L: List[AttachmentType] = List[AttachmentType](VAT5L)

  lazy val view: EmailCoverSheet = app.injector.instanceOf[EmailCoverSheet]

  object ExpectedContent {
    val heading = "How to email documents to HMRC"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "The subject line of the email must include your Register for VAT reference number. This will enable us to match your online application to your documents."
    val panel1 = s"Register for VAT reference number: $testRef"
    val heading2 = "What you must attach to the email"
    val para2 = "You must send us additional documents in order for us to process this VAT application:"
    val heading3 = "Email address"
    val vat2Bullet = "a completed VAT2 form (opens in new tab) to capture the details of all the partners"
    val vat51Bullet = "a completed VAT 50/51 form (opens in new tab) to provide us with details of the VAT group, including details of each subsidiary"
    val vat5LBullet = "a completed VAT5L form (opens in new tab)"
    val vat1614Bullet = "a completed VAT1614A (opens in new tab) or VAT1614H form (opens in new tab) if you have decided to, or want to opt to tax land or buildings"
    val supportingDocsBullet = "any supporting documents"
    val idEvidence = "three documents to confirm your identity"
    def idEvidenceNamed(name: String) = s"three documents to confirm $name’s identity"
    val para3 = "Send your documents to:"
    val panel2 = "VATREGBETA@hmrc.gov.uk"
    val print = "Print this page"
    val transactorName = "Transactor Name"
    val applicantName = "Applicant Name"
    val vat1TRBullet = "a completed VAT1TR form (opens in new tab)"
  }

  object IdentityEvidenceBlock {
    val summary = "What identity documents can I provide?"
    val content: String = "The three identity documents you should send " +
      "Include a copy of one piece of evidence that includes a government issued photo. This could be a: " +
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

  "The Email Cover Sheet page" must {
    implicit val doc: Document = Jsoup.parse(view(testRef, testAttachments, None, None).body)

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
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

    "not show the identity documents bullet point when attachment list does not contain IdentityEvidence" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(testRef, testVat2, None, None).body)

      doc.unorderedList(1) mustBe List(ExpectedContent.vat2Bullet)
      doc.unorderedList(1) mustNot contain(ExpectedContent.idEvidence)
    }

    "not show the vat51 bullet point when attachment list does not contain VAT51" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(testRef, testVat2, None, None).body)

      doc.unorderedList(1) mustBe List(ExpectedContent.vat2Bullet)
      doc.unorderedList(1) mustNot contain(ExpectedContent.vat51Bullet)
    }

    "not show the vat5L bullet point when attachment list does not contain VAT5L" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(testRef, testVat2, None, None).body)
      doc.unorderedList(1) mustBe List(ExpectedContent.vat2Bullet)
      doc.unorderedList(1) mustNot contain(ExpectedContent.vat5LBullet)
    }

    "have the correct first bullet list" in new ViewSetup {

      doc.unorderedList(1) mustBe List(
        ExpectedContent.vat2Bullet,
        ExpectedContent.vat51Bullet,
        ExpectedContent.idEvidence,
        ExpectedContent.vat5LBullet,
        ExpectedContent.vat1TRBullet
      )
    }

    "have the vat5L, vat1614 and supporting docs bullet list when OptionToTax feature switch is on and attachment list contains VAT5L" in new ViewSetup {
      enable(OptionToTax)
      override val doc: Document = Jsoup.parse(view(testRef, testVat5L, None, None).body)
      disable(OptionToTax)
      doc.unorderedList(1) mustBe List(
        ExpectedContent.vat5LBullet,
        ExpectedContent.vat1614Bullet,
        ExpectedContent.supportingDocsBullet
      )
    }

    "have the correct first bullet list for the transactor flow" when {
      "transactor is unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(testRef, List(TransactorIdentityEvidence), None, Some(ExpectedContent.transactorName)).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.idEvidenceNamed(ExpectedContent.transactorName)
        )
      }

      "applicant is unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(testRef, List(IdentityEvidence), Some(ExpectedContent.applicantName), None).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.idEvidenceNamed(ExpectedContent.applicantName)
        )
      }

      "both are unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(testRef, List(IdentityEvidence, TransactorIdentityEvidence), Some(ExpectedContent.applicantName), Some(ExpectedContent.transactorName)).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.idEvidenceNamed(ExpectedContent.applicantName),
          ExpectedContent.idEvidenceNamed(ExpectedContent.transactorName)
        )
      }
    }

    "have a details block" in new ViewSetup {
      doc.details mustBe Some(Details(IdentityEvidenceBlock.summary, IdentityEvidenceBlock.content))
    }

    "have the correct panel text two" in new ViewSetup {
      doc.select(Selectors.indent).get(1).text mustBe ExpectedContent.panel2
    }
  }
}

