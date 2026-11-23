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

package views.attachments

import forms.CoverLetterPrintedForm
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.PostalCoverSheetNewJourney

import scala.jdk.CollectionConverters._

class PostalCoverSheetNewJourneyViewSpec extends VatRegViewSpec {

  val testRef = "VRN 1234 5678"
  val allAttachments: List[AttachmentType] =
    List(VAT2, VAT51, VAT5L, TaxRepresentativeAuthorisation, IdentityEvidence)
  val applicantName = "Applicant Name"
  val transactorName = "Transactor Name"

  private def render(form: play.api.data.Form[Boolean] = CoverLetterPrintedForm(),
                     attachments: List[AttachmentType] = allAttachments,
                     applicant: Option[String] = None,
                     transactor: Option[String] = None)
                    (implicit msgs: play.api.i18n.Messages = messagesApi.preferred(request)): Document =
    Jsoup.parse(view(form, testRef, attachments, applicant, transactor)(request, msgs, appConfig).body)

  lazy val view: PostalCoverSheetNewJourney = app.injector.instanceOf[PostalCoverSheetNewJourney]

  object ExpectedContent {
    val heading = "VAT registration cover letter"
    val title = s"$heading - Register for VAT - GOV.UK"
    val errorTitle = s"Error: $title"
    val referenceKey = "This is your reference number:"
    val insetText = "This is your cover page. You need to print this off and post it with your forms and documents. This will help HMRC match your application with your documents."
    val para2 = "To process your VAT application, you need to post us:"
    val coverLetter = "this cover letter"
    val vat2 = "VAT2 form"
    val vat5L = "VAT5L form (include any supporting documents for this)"
    val vat51 = "VAT 50/51 form"
    val vat1TR = "VAT1TR form"
    val identity = "3 documents to confirm your identity"
    def namedIdentity(name: String) = s"3 documents to confirm $name’s identity"
    val requiredBullets = List(coverLetter, vat2, vat5L, vat51, vat1TR, identity)
    val para4 = "If you need to, you should also post:"
    val optionalBullets = List("VAT1614A form", "VAT1614H form")
    val warningText = "Do not post the original documents to HMRC as we are unable to return them to you."
    val proofOfIdentityHeading = "Proof of identity"
    val proofOfIdentityPara = "We need a copy of one of the following:"
    val proofOfIdentityBullets = List("your passport", "your photocard driving licence", "your national identity card")
    val otherIdentityHeading = "Other identity documents"
    val otherIdentityPara1 = "We also need copies of any 2 of the following:"
    val otherIdentityBullets = List(
      "your birth certificate",
      "a lease or rental agreement",
      "your work permit or visa",
      "a recent utility bill",
      "a recent mortgage statement",
      "a recent bank statement",
      "a recent credit card statement",
      "any documents from the Department for Work and Pensions that confirms your entitlement to benefits"
    )
    val otherIdentityPara2 = "Your documents should be dated within the last 3 months, where possible."
    val whereToSendHeading = "Where to send your forms"
    val whereToSendPara = "Post all forms and this cover page to:"
    val address = "VAT Registration Applications BT VAT HM Revenue and Customs BX9 1WR United Kingdom"
    val printOrSave = "Print or save this page as a PDF"
    val legend = "Have you printed or saved your cover letter?"
    val hint = "You will not be able to access this page again"
    val label = "I have printed or saved my cover letter"
    val button = "Finish"
    val error = "Select if you have printed or saved your cover letter"
  }

  // Only the Welsh reused from existing messages; the rest is still an English placeholder
  object WelshContent {
    val warningText = "Peidiwch â phostio’r dogfennau gwreiddiol at CThEM gan na allwn eu dychwelyd atoch."
    val vat1614Bullets = List("ffurflen VAT1614A wedi’i llenwi", "ffurflen VAT1614H wedi’i llenwi")
    val button = "Gorffen"
  }

  private def paragraphs(doc: Document): List[String] =
    doc.select("main p.govuk-body").asScala.toList.map(_.text)

  "The Postal Cover Sheet page (new attachment journey)" must {
    implicit val doc: Document = render()

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "show the acknowledgement reference in a tag next to its label" in new ViewSetup {
      doc.select(".govuk-summary-list__row dt").text mustBe ExpectedContent.referenceKey
      doc.select(".govuk-summary-list__row dd .govuk-tag").text mustBe testRef
      doc.select(".govuk-summary-list__row dd .govuk-tag").hasClass("govuk-phase-banner__content__tag") mustBe true
    }

    "have the correct inset text" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe ExpectedContent.insetText
    }

    "list every form and document when all of them are needed" in new ViewSetup {
      paragraphs(doc) must contain(ExpectedContent.para2)
      doc.unorderedList(1) mustBe ExpectedContent.requiredBullets
      paragraphs(doc) must contain(ExpectedContent.para4)
      doc.unorderedList(2) mustBe ExpectedContent.optionalBullets
    }

    "have the correct warning text" in new ViewSetup {
      doc.warningText(1) match {
        case Some(value) => value must include(ExpectedContent.warningText)
        case None => fail()
      }
    }

    "have the proof of identity section" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.proofOfIdentityHeading)
      paragraphs(doc) must contain(ExpectedContent.proofOfIdentityPara)
      doc.unorderedList(3) mustBe ExpectedContent.proofOfIdentityBullets
    }

    "have the other identity documents section" in new ViewSetup {
      doc.headingLevel2(2) mustBe Some(ExpectedContent.otherIdentityHeading)
      paragraphs(doc) must contain(ExpectedContent.otherIdentityPara1)
      doc.unorderedList(4) mustBe ExpectedContent.otherIdentityBullets
      paragraphs(doc) must contain(ExpectedContent.otherIdentityPara2)
    }

    "have the where to send section with the postal address" in new ViewSetup {
      doc.headingLevel2(3) mustBe Some(ExpectedContent.whereToSendHeading)
      paragraphs(doc) must contain(ExpectedContent.whereToSendPara)
      paragraphs(doc) must contain(ExpectedContent.address)
    }

    "render a single print or save button as a secondary button, hidden when printing" in new ViewSetup {
      val printDiv = doc.select("#print-button")
      printDiv.size mustBe 1
      printDiv.attr("data-content") mustBe ExpectedContent.printOrSave
      printDiv.attr("data-classes") mustBe "govuk-button govuk-button--secondary"
      printDiv.parents().hasClass("govuk-!-display-none-print") mustBe true
    }

    "have the printed question as the legend" in new ViewSetup {
      doc.select("legend").text mustBe ExpectedContent.legend
    }

    "have the correct hint" in new ViewSetup {
      doc.select(".govuk-hint").text mustBe ExpectedContent.hint
    }

    "have an unticked checkbox with the correct label" in new ViewSetup {
      val checkbox = doc.select("input[type=checkbox]#value")
      checkbox.attr("name") mustBe "value"
      checkbox.attr("value") mustBe "true"
      checkbox.hasAttr("checked") mustBe false
      doc.select("label[for=value]").text mustBe ExpectedContent.label
    }

    "post the form back to the postal cover sheet url" in new ViewSetup {
      doc.select("form").attr("action") mustBe controllers.attachments.routes.PostalCoverSheetController.submit.url
    }

    "have the Finish button" in new ViewSetup {
      doc.select("button.govuk-button").text mustBe ExpectedContent.button
    }

    "hide the question from print" in new ViewSetup {
      doc.select("form").parents().hasClass("govuk-!-display-none-print") mustBe true
    }

    "not show an error summary" in new ViewSetup {
      doc.select(".govuk-error-summary").size mustBe 0
    }
  }

  "The list of what to post (new attachment journey)" when {
    "only identity evidence is needed" must {
      implicit val doc: Document = render(attachments = List(IdentityEvidence))

      "list only the cover letter and identity documents" in new ViewSetup {
        doc.unorderedList(1) mustBe List(ExpectedContent.coverLetter, ExpectedContent.identity)
      }

      "not show the VAT1614A/H list" in new ViewSetup {
        paragraphs(doc) mustNot contain(ExpectedContent.para4)
        doc.select("main ul").text mustNot include("VAT1614")
      }

      "show the identity sections" in new ViewSetup {
        doc.headingLevel2(1) mustBe Some(ExpectedContent.proofOfIdentityHeading)
        doc.headingLevel2(2) mustBe Some(ExpectedContent.otherIdentityHeading)
      }
    }

    "no identity evidence is needed" must {
      implicit val doc: Document = render(attachments = List(VAT2))

      "list only the cover letter and the VAT2 form" in new ViewSetup {
        doc.unorderedList(1) mustBe List(ExpectedContent.coverLetter, ExpectedContent.vat2)
      }

      "not show the identity sections" in new ViewSetup {
        doc.select("main h2").eachText() mustNot contain(ExpectedContent.proofOfIdentityHeading)
        doc.select("main h2").eachText() mustNot contain(ExpectedContent.otherIdentityHeading)
        doc.headingLevel2(1) mustBe Some(ExpectedContent.whereToSendHeading)
      }
    }

    "a VAT5L is needed" must {
      implicit val doc: Document = render(attachments = List(VAT5L))

      "list the VAT5L form and the optional VAT1614A/H forms" in new ViewSetup {
        doc.unorderedList(1) mustBe List(ExpectedContent.coverLetter, ExpectedContent.vat5L)
        paragraphs(doc) must contain(ExpectedContent.para4)
        doc.unorderedList(2) mustBe ExpectedContent.optionalBullets
      }
    }

    "the attachments are in a different order" must {
      implicit val doc: Document = render(attachments = List(IdentityEvidence, TaxRepresentativeAuthorisation, VAT51, VAT2))

      "still list them in the fixed order" in new ViewSetup {
        doc.unorderedList(1) mustBe List(
          ExpectedContent.coverLetter, ExpectedContent.vat2, ExpectedContent.vat51, ExpectedContent.vat1TR, ExpectedContent.identity
        )
      }
    }

    "it is a transactor application" must {
      "name the applicant" in new ViewSetup()(render(attachments = List(IdentityEvidence), applicant = Some(applicantName), transactor = Some(transactorName))) {
        doc.unorderedList(1) mustBe List(ExpectedContent.coverLetter, ExpectedContent.namedIdentity(applicantName))
      }

      "name both the applicant and the transactor when both need identity evidence" in new ViewSetup()(
        render(attachments = List(TransactorIdentityEvidence, IdentityEvidence), applicant = Some(applicantName), transactor = Some(transactorName))
      ) {
        doc.unorderedList(1) mustBe List(
          ExpectedContent.coverLetter, ExpectedContent.namedIdentity(applicantName), ExpectedContent.namedIdentity(transactorName)
        )
      }

      "show the identity sections when only the transactor needs identity evidence" in new ViewSetup()(
        render(attachments = List(TransactorIdentityEvidence), applicant = Some(applicantName), transactor = Some(transactorName))
      ) {
        doc.unorderedList(1) mustBe List(ExpectedContent.coverLetter, ExpectedContent.namedIdentity(transactorName))
        doc.headingLevel2(1) mustBe Some(ExpectedContent.proofOfIdentityHeading)
      }
    }
  }

  "The Postal Cover Sheet page (new attachment journey) with errors" must {
    implicit val doc: Document = render(CoverLetterPrintedForm().bind(Map.empty[String, String]))

    "have an error title" in new ViewSetup {
      doc.title mustBe ExpectedContent.errorTitle
    }

    "show the error summary linking to the checkbox" in new ViewSetup {
      doc.select(".govuk-error-summary__list a").text mustBe ExpectedContent.error
      doc.select(".govuk-error-summary__list a").attr("href") mustBe "#value"
    }

    "show the inline error message" in new ViewSetup {
      doc.select(".govuk-error-message").text must include(ExpectedContent.error)
    }

    "hide the error summary and the inline error when printing" in new ViewSetup {
      doc.select(".govuk-error-summary").hasClass("govuk-!-display-none-print") mustBe true
      doc.select(".govuk-error-message").parents().hasClass("govuk-!-display-none-print") mustBe true
    }

    "keep the checkbox ticked when it was ticked" in new ViewSetup {
      val ticked = render(CoverLetterPrintedForm().fill(true))
      ticked.select("input[type=checkbox]#value").hasAttr("checked") mustBe true
    }
  }

  "The Postal Cover Sheet page (new attachment journey) in Welsh" must {
    implicit val doc: Document = render(CoverLetterPrintedForm().bind(Map.empty[String, String]))(welshMessages)

    "have the Welsh warning text" in new ViewSetup {
      doc.warningText(1) match {
        case Some(value) => value must include(WelshContent.warningText)
        case None => fail()
      }
    }

    "have the Welsh VAT1614A/H bullets" in new ViewSetup {
      // list 1 is the error summary on this page
      doc.unorderedList(3) mustBe WelshContent.vat1614Bullets
    }

    "have the Welsh Finish button" in new ViewSetup {
      doc.select("button.govuk-button").text mustBe WelshContent.button
    }
  }
}
