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

package views.attachments

import featureswitch.core.config._
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.MultipleDocumentsRequired

class MultipleDocumentsRequiredViewSpec extends VatRegViewSpec with FeatureSwitching {

  val view: MultipleDocumentsRequired = app.injector.instanceOf[MultipleDocumentsRequired]

  object ExpectedContent {
    val heading = "You must send us additional documents in order for us to process this VAT application"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "To enable us to progress your application further, we need the following information and documents from you:"
    val bullet1 = "identity documents of you"
    def bullet1Named(name: String) = s"three documents to confirm $name’s identity"
    val linkText = "VAT2 form (opens in new tab)"
    val bullet2 = s"a completed $linkText"
    val vat5LBullet = "a completed VAT5L form (opens in new tab) with details of the land and property supplies the business is making"
    val vat1614Bullet = "a VAT1614A (opens in new tab) or VAT1614H form (opens in new tab) if you have decided to, or want to opt to tax land or buildings"
    val supportingDocsBullet = "any supporting documents"
    val continue = "Continue"
    val transactorName = "Transactor Name"
    val applicantName = "Applicant Name"
    val vat1TRBullet = "a completed VAT1TR form (opens in new tab) with details of your chosen UK tax representative."
  }

  object IdentityDetails {
    val summary = "What identity documents can I provide?"
    val content: String = "We need one document to verify your identity. The document must have a government issued photo, for example: " +
      "a passport " +
      "a photocard driving licence " +
      "a national identity card " +
      "We need two additional pieces of evidence which can be copies of: " +
      "a mortgage statement " +
      "a lease or rental agreement " +
      "a work permit or Visa " +
      "any correspondence from the Department for Work and Pensions confirming entitlement to benefits " +
      "a recent utility bill " +
      "a birth certificate"
  }

  "The Multiple Documents Required page" must {
    implicit val doc: Document = Jsoup.parse(view(List(IdentityEvidence, VAT2), None, None).body)

    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2
      )
    }

    "not show the vat5L bullet point when attachment list does not contain VAT5L" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(List(IdentityEvidence), None, None).body)

      doc.unorderedList(1) mustNot contain(ExpectedContent.vat5LBullet)
    }

    "show the vat5L bullet point when attachment list does contain VAT5L" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(List(VAT5L), None, None).body)

      doc.unorderedList(1) must contain(ExpectedContent.vat5LBullet)
    }

    "show the vat5L, vat1614 and supporting docs bullet points when option to tax feature switch is on and attachment list contains VAT5L" in new ViewSetup {
      enable(OptionToTax)
      override val doc: Document = Jsoup.parse(view(List(VAT5L), None, None).body)
      disable(OptionToTax)

      doc.unorderedList(1) must contain(ExpectedContent.vat5LBullet)
      doc.unorderedList(1) must contain(ExpectedContent.vat1614Bullet)
      doc.unorderedList(1) must contain(ExpectedContent.supportingDocsBullet)
    }

    "show the vat1TR bullet point when attachment list does contain vat1TR" in new ViewSetup {
      override val doc: Document = Jsoup.parse(view(List(TaxRepresentativeAuthorisation), None, None).body)

      doc.unorderedList(1) must contain(ExpectedContent.vat1TRBullet)
    }

    "have the correct bullet list for the transactor flow" when {
      "transactor is unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(List(TransactorIdentityEvidence), None, Some(ExpectedContent.transactorName)).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.bullet1Named(ExpectedContent.transactorName)
        )
      }

      "applicant is unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(List(IdentityEvidence), Some(ExpectedContent.applicantName), None).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.bullet1Named(ExpectedContent.applicantName)
        )
      }

      "both are unverified" in new ViewSetup {
        override val doc: Document = Jsoup.parse(view(List(IdentityEvidence, TransactorIdentityEvidence), Some(ExpectedContent.applicantName), Some(ExpectedContent.transactorName)).body)

        doc.unorderedList(1) mustBe List(
          ExpectedContent.bullet1Named(ExpectedContent.applicantName),
          ExpectedContent.bullet1Named(ExpectedContent.transactorName)
        )
      }
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, appConfig.vat2Link))
    }

    "have a details block" in new ViewSetup {
      doc.details mustBe Some(Details(IdentityDetails.summary, IdentityDetails.content))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
