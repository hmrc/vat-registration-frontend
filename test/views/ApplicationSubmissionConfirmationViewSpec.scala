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

package views

import featuretoggle.FeatureToggleSupport
import models._
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ApplicationSubmissionConfirmation

class ApplicationSubmissionConfirmationViewSpec extends VatRegViewSpec with FeatureToggleSupport {

  val refNum = "VRS 1234 1234 1234"
  val refNumNew = "1234 1234 1234"
  val refNumAgentPost = "1234 1234 1234 Give this number to the business. They need it to access their VAT account."

  val testEmail = "testEmail@mail.gov.uk"

  val howToRegisterUrl = "https://www.gov.uk/register-for-vat/how-register-for-vat"
  val togcVatNoticeUrl = "https://www.gov.uk/guidance/transfer-a-business-as-a-going-concern-and-vat-notice-7009"

  def assertNewTabGovukLink(doc: Document, href: String, text: String): Unit = {
    val links = doc.select(s"a[href=$href]")
    links.size mustBe 1
    links.first.text mustBe text
    links.first.hasClass("govuk-link") mustBe true
    links.first.attr("target") mustBe "_blank"
    links.first.attr("rel") mustBe "noreferrer"
  }

  object ExpectedContent {
    val title = "Your application has been submitted - Register for VAT - GOV.UK"
    val heading = "Your application has been submitted"
    val ackRef = "Your reference number VRS 1234 1234 1234"
    val ackRefNew = "VAT application number: 1234 1234 1234 You will need this number to access your VAT account."
    val ackRefAgentPost = "VAT application number: 1234 1234 1234 Give this number to the business. They need it to access their VAT account."
    val emailPara = s"We have emailed the information on this page to $testEmail."
    val emailParaNew = s"The information on this page has been sent to $testEmail."

    val heading2 = "What happens next"
    val listItem1 = "We’ve received your application and we will write to you with a decision within 40 working days."
    val listItem1New = "After we receive your documents, we will write to you with a decision on your application in 40 working days."
    val listItemPost1 = "We have received your online application. So we can process it, post us a cover letter and copies of the required documents."
    val opensInNewTab = "(opens in new tab)"

    val listItemAgentPost  = "We have received the business’s online application. So we can process it, post us a cover letter and copies of the required documents."
    val postCoverSheetLinkText = s"post us a cover letter and copies of the required documents $opensInNewTab"
    val listItemPost1WithLink = s"We have received your online application. So we can process it, $postCoverSheetLinkText."
    val listItemAgentPostWithLink = s"We have received the business’s online application. So we can process it, $postCoverSheetLinkText."
    val listItem1Doc = "After we receive copies of your documents, we’ll write to you with a decision on your application within 40 working days."
    val listItem2 = "You should wait until we confirm your VAT registration before you get software to follow the rules for ‘Making Tax Digital for VAT’."
    val listItem2New = "You should wait until we confirm your VAT registration before you get software to follow the rules for ‘Making Tax Digital for VAT’."
    val listItemDigitalVatGroup = "After we receive the business’s documents, we will send you a decision on the application in 40 working days."


    val whatHappensNextListItem1 = "After we receive the business’s documents, we will write to you (the agent or third party) with a decision on the application in 40 working days."
    val whatHappensNextListItem2 = "The VAT group should not start using accounting software to manage Making Tax Digital for VAT records before VAT registration has been confirmed."

    val whatHappensNextListItemVATGroup = "A VAT group should not start using accounting software to manage Making Tax Digital for VAT records before VAT registration has been confirmed."
    val whatHappensNextListItemTogc = "Do not start using accounting software to manage your VAT records and file returns until VAT registration has been confirmed."
    val whatHappensNextListItemAgentTogc = "Do not start using accounting software to manage the business’s VAT records and file returns until VAT registration has been confirmed."
    val tradeBeforeListItem1 = "VAT cannot be included on the business’s invoices until it has a VAT registration number but prices can be increased to account for the VAT that will be owed. The extra that has been charged for VAT can then be used to pay HMRC."
    val tradeBeforeListItemVATGroup = "VAT cannot be included on your invoices until you have a VAT registration number but prices can be increased to account for the VAT that will be owed. The extra that has been charged for VAT can then be used to pay HMRC."

    val tradeBeforeListItem1Togc = "VAT cannot be included on your invoices until it has a VAT registration number but prices can be increased to account for the VAT that will be owed. The extra that has been charged for VAT can then be used to pay HMRC. Visit Register for VAT to find out more about ‘Accounting for VAT while you wait for your VAT registration number’."
    val tradeBeforeListItem1AgentTogc = "VAT cannot be included on the business’s invoices until it has a VAT registration number but prices can be increased to account for the VAT that will be owed. The extra that has been charged for VAT can then be used to pay HMRC. Visit Register for VAT to find out more about ‘Accounting for VAT while you wait for your VAT registration number’."
    val tradeBeforeListItem2Togc = "If a company, or part of a company, has been sold and treated as a transfer of a going concern (TOGC) then no VAT is chargeable, subject to certain conditions."

    val vatGroupListItem2 = "The VAT group should wait until we confirm its VAT registration before getting software to follow the rules for ‘Making Tax Digital for VAT’."
    val listItem23pt = "The business should wait until its VAT registration is confirmed before getting software to follow the rules for ‘Making Tax Digital for VAT’."
    val insetLink = "https://www.gov.uk/register-for-vat/how-register-for-vat"
    val insetLinkText = "account for the VAT you’ll need to pay HMRC (opens in new tab)"
    val registerLinkText = "Register for VAT"
    val insetText = s"You cannot include VAT on your invoices until you get your VAT number. However, you can increase your prices to $insetLinkText."
    val insetLinkText3pt = "account for the VAT it needs to pay HMRC (opens in new tab)"
    val insetTextTOGC = s"If you are getting a new VAT number, you cannot include VAT on your invoices until you receive this. However, you can increase your prices to $insetLinkText."
    val insetTextVatGroup = s"The VAT group cannot include VAT on its invoices until it gets its VAT number. However, it can increase its prices to $insetLinkText3pt."
    val insetText3pt = s"The business cannot include VAT on its invoices until it gets its VAT number. However, it can increase its prices to $insetLinkText3pt."
    val insetTextTOGC3pt = s"If the business is getting a new VAT number, it cannot include VAT on its invoices until it receives this. However, it can increase its prices to $insetLinkText3pt."
    val buttonText = "Finish"
    val firstIndentText = "The VAT application number is different to the VAT registration number."
    val docHeading = "What you must do now"
    val docPostalText = "Postal address"
    val docPostLink: String = controllers.attachments.routes.PostalCoverSheetController.show.url
    val docPostLinkText = "post us a cover letter and copies of your documents (opens in new tab)"
    val docPostLinkText3pt = s"post us a cover letter and copies of the required documents $opensInNewTab"
    val docPostText = s"We’ve received your application. So we can process it, $docPostLinkText."
    val docPostText3pt = s"We’ve received your application. So we can process it, $docPostLinkText3pt."
    val postCoverSheetLink = "/register-for-vat/postal-cover-sheet"
    val tradeBeforeDigitalVatGroupListItem = "Visit Register for VAT to find out more about ‘Accounting for VAT while you wait for your VAT registration number’."
  }

  val viewInstance: ApplicationSubmissionConfirmation = app.injector.instanceOf[ApplicationSubmissionConfirmation]

  "Application submission confirmation page" must {
    lazy val view = viewInstance(refNum, None, attachmentsListExists = false, testEmail, isTransactor = false, registrationReason = ForwardLook)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.firstIndentText)
    }

    "have the correct paragraph" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.emailParaNew)
    }

    "have the correct list" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItem1New)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.registerLinkText, ExpectedContent.insetLink))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Post attachment method" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = false, registrationReason = ForwardLook)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.firstIndentText)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.docPostalText)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRefNew
    }

    "have the correct paragraph" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.emailParaNew)
    }

    "have the correct list" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemPost1WithLink)
    }

    "link to the postal cover sheet when VrsNewAttachmentJourney is disabled" in new ViewSetup {
      val links = doc.select(s"a[href=${ExpectedContent.postCoverSheetLink}]")
      links.size mustBe 1
      links.first.text mustBe ExpectedContent.postCoverSheetLinkText
      links.first.attr("target") mustBe "_blank"
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Application submission confirmation page with Post attachment method for a transactor" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = true, registrationReason = ForwardLook)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.firstIndentText)
    }

    "have the correct subheadings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.docHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedContent.docPostalText)
      doc.select(".govuk-panel__body").text() mustBe ExpectedContent.ackRefAgentPost
    }

   "have the correct paragraphs" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.emailParaNew)
    }

    "have the correct list" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemAgentPostWithLink)
    }

    "link to the postal cover sheet when VrsNewAttachmentJourney is disabled" in new ViewSetup {
      val links = doc.select(s"a[href=${ExpectedContent.postCoverSheetLink}]")
      links.size mustBe 1
      links.first.text mustBe ExpectedContent.postCoverSheetLinkText
      links.first.attr("target") mustBe "_blank"
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

  "Confirmation page with attachment method/VatRegReason/isTransactor(Agent) => Post/VATGroup/True" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = true, registrationReason = GroupRegistration)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct list" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemAgentPostWithLink)
    }

    "link to the postal cover sheet when VrsNewAttachmentJourney is disabled" in new ViewSetup {
      val links = doc.select(s"a[href=${ExpectedContent.postCoverSheetLink}]")
      links.size mustBe 1
      links.first.text mustBe ExpectedContent.postCoverSheetLinkText
      links.first.attr("target") mustBe "_blank"
    }

    "have the correct list in whatHappensNext Section" in new ViewSetup {
      doc.para(6) mustBe Some(ExpectedContent.whatHappensNextListItem1)
      doc.para(7) mustBe Some(ExpectedContent.whatHappensNextListItem2)
    }

   "have the correct list in TradeBefore Section" in new ViewSetup {
      doc.para(8) mustBe Some(ExpectedContent.tradeBeforeListItem1)
    }
  }


  "Confirmation page with Post attachment method when VrsNewAttachmentJourney is enabled" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = false, registrationReason = ForwardLook, isNewAttachmentJourney = true)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the cover letter guidance as plain text" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemPost1)
    }

    "not link to the postal cover sheet" in new ViewSetup {
      doc.select(s"a[href=${ExpectedContent.postCoverSheetLink}]").isEmpty mustBe true
    }
  }

  "Confirmation page with Post attachment method for a transactor when VrsNewAttachmentJourney is enabled" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = true, registrationReason = GroupRegistration, isNewAttachmentJourney = true)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the cover letter guidance as plain text" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemAgentPost)
    }

    "not link to the postal cover sheet" in new ViewSetup {
      doc.select(s"a[href=${ExpectedContent.postCoverSheetLink}]").isEmpty mustBe true
    }
  }

  "Confirmation page with attachment method/VatRegReason/isTransactor(Agent) => Post/VATGroup/False" must {
    lazy val view = viewInstance(refNumNew, Some(Post), attachmentsListExists = true, testEmail, isTransactor = false, registrationReason = GroupRegistration)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct list in whatHappensNext Section" in new ViewSetup {
      doc.para(7) mustBe Some(ExpectedContent.whatHappensNextListItemVATGroup)
    }

    "have the correct list in TradeBefore Section" in new ViewSetup {
      doc.para(8) mustBe Some(ExpectedContent.tradeBeforeListItemVATGroup)
    }

    "have a styled Register for VAT link opening in a new tab" in new ViewSetup {
      assertNewTabGovukLink(doc, howToRegisterUrl, "Register for VAT")
    }
  }

  "Confirmation page with attachment method/VatRegReason/isTransactor(Agent) => Digital/VATGroup/True" must {
    lazy val view = viewInstance(refNumNew, Some(Upload), attachmentsListExists = true, testEmail, isTransactor = true, registrationReason = GroupRegistration)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct list in whatHappensNext Section" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.listItemDigitalVatGroup)
    }

    "have the correct list in TradeBefore Section" in new ViewSetup {
      doc.para(6) mustBe Some(ExpectedContent.tradeBeforeDigitalVatGroupListItem)
    }
  }

  "Confirmation page with attachment method/VatRegReason/isTransactor(Agent) => Digital/TOGC/True" must {
    lazy val view = viewInstance(refNumNew, Some(Upload), attachmentsListExists = false, testEmail, isTransactor = true, registrationReason = TransferOfAGoingConcern)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct list in whatHappensNext Section" in new ViewSetup {
      doc.para(4) mustBe Some(ExpectedContent.whatHappensNextListItemAgentTogc)
    }

    "have the correct list in TradeBefore Section" in new ViewSetup {
      doc.para(5) mustBe Some(ExpectedContent.tradeBeforeListItem1AgentTogc)
      doc.para(6) mustBe Some(ExpectedContent.tradeBeforeListItem2Togc)
    }

    "have a styled Register for VAT link opening in a new tab" in new ViewSetup {
      assertNewTabGovukLink(doc, howToRegisterUrl, "Register for VAT")
    }

    "have a styled TOGC VAT notice link opening in a new tab" in new ViewSetup {
      assertNewTabGovukLink(doc, togcVatNoticeUrl, "treated as a transfer of a going concern")
    }
  }

  "Confirmation page with attachment method/VatRegReason/isTransactor(Agent) => Digital/TOGC/False" must {
    lazy val view = viewInstance(refNumNew, Some(Upload), attachmentsListExists = false, testEmail, isTransactor = false, registrationReason = TransferOfAGoingConcern)
    implicit lazy val doc: Document = Jsoup.parse(view.body)

    "have the correct list in whatHappensNext Section" in new ViewSetup {
      doc.para(4) mustBe Some(ExpectedContent.whatHappensNextListItemTogc)
    }

    "have the correct list in TradeBefore Section" in new ViewSetup {
      doc.para(5) mustBe Some(ExpectedContent.tradeBeforeListItem1Togc)
      doc.para(6) mustBe Some(ExpectedContent.tradeBeforeListItem2Togc)
    }

    "have a styled Register for VAT link opening in a new tab" in new ViewSetup {
      assertNewTabGovukLink(doc, howToRegisterUrl, "Register for VAT")
    }

    "have a styled TOGC VAT notice link opening in a new tab" in new ViewSetup {
      assertNewTabGovukLink(doc, togcVatNoticeUrl, "treated as a transfer of a going concern")
    }
  }
}