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

import models.api.{EmailMethod, Post}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ApplicationSubmissionConfirmation

class ApplicationSubmissionConfirmationViewSpec extends VatRegViewSpec {

  val refNum = "VRS 1234 1234 1234"
  val testEmail = "testEmail@mail.gov.uk"

  object ExpectedContent {
    val title = "Your application has been submitted - Register for VAT - GOV.UK"
    val heading = "Your application has been submitted"
    val ackRef = "Your reference number VRS 1234 1234 1234"
    val emailPara = s"We have emailed the information on this page to $testEmail."
    val heading2 = "What happens next"
    val listItem1 = "We’ve received your application and we will write to you with a decision in about 30 days."
    val listItem1Doc = "After we receive copies of your documents, we’ll write to you with a decision on your application in about 30 days."
    val listItem2 = "You should wait until we confirm your VAT registration before you get software to follow the rules for ‘Making Tax Digital for VAT’."
    val listItem23pt = "The business should wait until its VAT registration is confirmed before getting software to follow the rules for ‘Making Tax Digital for VAT’."
    val insetLink = "https://www.gov.uk/register-for-vat/how-register-for-vat"
    val insetLinkText = "account for the VAT you’ll need to pay HMRC (opens in new tab)"
    val insetText = s"You cannot include VAT on your invoices until you get your VAT number. However, you can increase your prices to $insetLinkText."
    val insetLinkText3pt = "account for the VAT it needs to pay HMRC (opens in new tab)"
    val insetText3pt = s"The business cannot include VAT on its invoices until it gets its VAT number. However, it can increase its prices to $insetLinkText3pt."
    val buttonText = "Finish"

    val docHeading = "What you must do now"
    val docPostLink: String = controllers.attachments.routes.PostalCoverSheetController.show.url
    val docPostLinkText = "post us a cover letter and copies of your documents (opens in new tab)"
    val docPostLinkText3pt = "post us a cover letter and copies of the required documents (opens in new tab)"
    val docPostText = s"We’ve received your application. So we can process it, $docPostLinkText."
    val docPostText3pt = s"We’ve received your application. So we can process it, $docPostLinkText3pt."
    val docEmailLink: String = controllers.attachments.routes.EmailCoverSheetController.show.url
    val docEmailLinkText = "email us copies of your documents (opens in new tab)"
    val docEmailLinkText3pt = "email us copies of the required documents (opens in new tab)"
    val docEmailText = s"We’ve received your application. So we can process it, $docEmailLinkText."
    val docEmailText3pt = s"We’ve received your application. So we can process it, $docEmailLinkText3pt."
  }

  val viewInstance: ApplicationSubmissionConfirmation = app.injector.instanceOf[ApplicationSubmissionConfirmation]

  "Application submission confirmation page" must {
    lazy val view = viewInstance(refNum, None, attachmentsListExists = false, testEmail, isTransactor = false)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.heading2)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRef
    }

    "have the correct paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.emailPara)
    }

    "have the correct list" in new ViewSetup {
      doc.orderedList(1) mustBe List(ExpectedContent.listItem1, ExpectedContent.listItem2)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.insetText)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.insetLinkText, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Post attachment method" must {
    lazy val view = viewInstance(refNum, Some(Post), attachmentsListExists = true, testEmail, isTransactor = false)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.heading2)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRef
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.emailPara)
      doc.para(2) mustBe Some(ExpectedContent.docPostText)
    }

    "have the correct list" in new ViewSetup {
      doc.orderedList(1) mustBe List(ExpectedContent.listItem1Doc, ExpectedContent.listItem2)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.insetText)
    }

    "have the correct links" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.docPostLinkText, ExpectedContent.docPostLink))
      doc.link(2) mustBe Some(Link(ExpectedContent.insetLinkText, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Email attachment method" must {
    lazy val view = viewInstance(refNum, Some(EmailMethod), attachmentsListExists = true, testEmail, isTransactor = false)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.heading2)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRef
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.emailPara)
      doc.para(2) mustBe Some(ExpectedContent.docEmailText)
    }

    "have the correct list" in new ViewSetup {
      doc.orderedList(1) mustBe List(ExpectedContent.listItem1Doc, ExpectedContent.listItem2)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.insetText)
    }

    "have the correct links" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.docEmailLinkText, ExpectedContent.docEmailLink))
      doc.link(2) mustBe Some(Link(ExpectedContent.insetLinkText, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Post attachment method for a transactor" must {
    lazy val view = viewInstance(refNum, Some(Post), attachmentsListExists = true, testEmail, isTransactor = true)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.heading2)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRef
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.emailPara)
      doc.para(2) mustBe Some(ExpectedContent.docPostText3pt)
    }

    "have the correct list" in new ViewSetup {
      doc.orderedList(1) mustBe List(ExpectedContent.listItem1Doc, ExpectedContent.listItem23pt)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.insetText3pt)
    }

    "have the correct links" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.docPostLinkText3pt, ExpectedContent.docPostLink))
      doc.link(2) mustBe Some(Link(ExpectedContent.insetLinkText3pt, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Email attachment method for a transactor" must {
    lazy val view = viewInstance(refNum, Some(EmailMethod), attachmentsListExists = true, testEmail, isTransactor = true)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.heading2)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRef
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.emailPara)
      doc.para(2) mustBe Some(ExpectedContent.docEmailText3pt)
    }

    "have the correct list" in new ViewSetup {
      doc.orderedList(1) mustBe List(ExpectedContent.listItem1Doc, ExpectedContent.listItem23pt)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.insetText3pt)
    }

    "have the correct links" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.docEmailLinkText3pt, ExpectedContent.docEmailLink))
      doc.link(2) mustBe Some(Link(ExpectedContent.insetLinkText3pt, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }
}