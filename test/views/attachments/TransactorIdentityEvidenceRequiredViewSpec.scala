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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.TransactorIdentityEvidenceRequired

class TransactorIdentityEvidenceRequiredViewSpec extends VatRegViewSpec {

  val view: TransactorIdentityEvidenceRequired = app.injector.instanceOf[TransactorIdentityEvidenceRequired]

  object ExpectedContent {
    def heading(transactorName: Option[String], applicantName: Option[String]): String = (transactorName, applicantName) match {
      case (Some(transactor), Some(applicant)) => s"You must send us three identity documents for $transactor and $applicant in order for us to process this VAT application"
      case (Some(transactor), None) => s"You must send us three identity documents for $transactor in order for us to process this VAT application"
      case (None, Some(applicant)) => s"You must send us three identity documents for $applicant in order for us to process this VAT application"
    }
    def title(headingText: String) = s"$headingText - Register for VAT - GOV.UK"
    def para1(transactorName: Option[String], applicantName: Option[String]): String = (transactorName, applicantName) match {
      case (Some(transactor), Some(applicant)) => s"We need one document to verify both $transactor and $applicant’s identity, this must include a government issued photo for example:"
      case (Some(transactor), None) => s"We need one document to verify $transactor’s identity, this must include a government issued photo for example:"
      case (None, Some(applicant)) => s"We need one document to verify $applicant’s identity, this must include a government issued photo for example:"
    }
    val bullet1 = "a passport"
    val bullet2 = "a photo drivers licence"
    val bullet3 = "a national identity card"
    val para2 = "We need two additional pieces of evidence which can be copies of:"
    val additionalBullet1 = "a mortgage statement"
    val additionalBullet2 = "a lease or rental agreement"
    val additionalBullet3 = "a work permit or Visa"
    val additionalBullet4 = "any correspondence from the Department for Work and Pensions confirming entitlement to benefits"
    val additionalBullet5 = "a recent utility bill"
    val additionalBullet6 = "a birth certificate"
    val continue = "Save and continue"
    val transactorName = "Transactor Name"
    val applicantName = "Applicant Name"
  }

  "The transactor identity evidence required page for both transactor and applicant" must {
    implicit val doc: Document = Jsoup.parse(view(List(ExpectedContent.transactorName, ExpectedContent.applicantName)).body)

    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading(Some(ExpectedContent.transactorName), Some(ExpectedContent.applicantName)))
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title(ExpectedContent.heading(Some(ExpectedContent.transactorName), Some(ExpectedContent.applicantName)))
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1(Some(ExpectedContent.transactorName), Some(ExpectedContent.applicantName)))
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2,
        ExpectedContent.bullet3
      )
    }

    "have the correct additional bullet list" in new ViewSetup {
      doc.unorderedList(2) mustBe List(
        ExpectedContent.additionalBullet1,
        ExpectedContent.additionalBullet2,
        ExpectedContent.additionalBullet3,
        ExpectedContent.additionalBullet4,
        ExpectedContent.additionalBullet5,
        ExpectedContent.additionalBullet6
      )
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The transactor identity evidence required page for transactor" must {
    implicit val doc: Document = Jsoup.parse(view(List(ExpectedContent.transactorName)).body)

    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading(Some(ExpectedContent.transactorName), None))
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title(ExpectedContent.heading(Some(ExpectedContent.transactorName), None))
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1(Some(ExpectedContent.transactorName), None))
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2,
        ExpectedContent.bullet3
      )
    }

    "have the correct additional bullet list" in new ViewSetup {
      doc.unorderedList(2) mustBe List(
        ExpectedContent.additionalBullet1,
        ExpectedContent.additionalBullet2,
        ExpectedContent.additionalBullet3,
        ExpectedContent.additionalBullet4,
        ExpectedContent.additionalBullet5,
        ExpectedContent.additionalBullet6
      )
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The transactor identity evidence required page for applicant" must {
    implicit val doc: Document = Jsoup.parse(view(List(ExpectedContent.applicantName)).body)

    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading(None, Some(ExpectedContent.applicantName)))
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title(ExpectedContent.heading(None, Some(ExpectedContent.applicantName)))
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1(None, Some(ExpectedContent.applicantName)))
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2,
        ExpectedContent.bullet3
      )
    }

    "have the correct additional bullet list" in new ViewSetup {
      doc.unorderedList(2) mustBe List(
        ExpectedContent.additionalBullet1,
        ExpectedContent.additionalBullet2,
        ExpectedContent.additionalBullet3,
        ExpectedContent.additionalBullet4,
        ExpectedContent.additionalBullet5,
        ExpectedContent.additionalBullet6
      )
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
