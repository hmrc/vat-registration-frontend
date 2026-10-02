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

package views.fileupload

import models.api._
import models.external.upscan.UpscanResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.UploadDocumentNewJourney

class UploadDocumentNewJourneyViewSpec extends VatRegViewSpec {

  val uploadDocumentsPage: UploadDocumentNewJourney = app.injector.instanceOf[UploadDocumentNewJourney]
  val testReference = "testReference"
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  object ExpectedContent {
    val heading = "Upload a document"
    val headingSupporting = "Upload a supporting document"
    val heading1614a = "Upload a VAT1614A"
    val title = s"$heading - Register for VAT - GOV.UK"
    val titleSupporting = s"$headingSupporting - Register for VAT - GOV.UK"
    val title1614a = s"$heading1614a - Register for VAT - GOV.UK"
    val testHint = "testHint"
    val fileTypeHint = "The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val label = "Upload a file"
    val continue = "Continue"
    val upload = "Upload"
    val fileUploadError = "Error: The selected file must be smaller than 10MB"
    val genericFileUploadError = "Error: The selected file could not be uploaded"
  }

  case class FormPage(attachmentType: AttachmentType, form: String, linkHref: String) {
    val heading = s"Upload your $form form"
    val linkText = s"Download and find out more about $form forms (opens in a new tab)"
    val welshHeading = s"Uwchlwytho’ch ffurflen $form"
    val welshLinkText = s"Lawrlwythwch ffurflen $form a dysgu rhagor amdani (yn agor tab newydd)"
  }

  val formPages: Seq[FormPage] = Seq(
    FormPage(VAT2, "VAT2", "https://www.gov.uk/government/publications/vat-partnership-details-vat2"),
    FormPage(VAT51, "VAT 50/51", "https://www.gov.uk/government/publications/apply-for-vat-group-registration-or-amend-your-details"),
    FormPage(TaxRepresentativeAuthorisation, "VAT1TR", "https://www.gov.uk/government/publications/vat-appointment-of-tax-representative-vat1tr"),
    FormPage(VAT5L, "VAT5L", "https://www.gov.uk/guidance/tell-hmrc-about-land-and-property-supplies-youre-making")
  )

  formPages.foreach { page =>

    s"The Upload Documents Page for ${page.attachmentType}" must {
      lazy val view: Html = uploadDocumentsPage(
        testUpscanResponse,
        Some(Html(ExpectedContent.testHint)),
        page.attachmentType,
        None
      )
      implicit val doc: Document = Jsoup.parse(view.body)

      "have the correct heading" in new ViewSetup {
        doc.heading mustBe Some(page.heading)
      }

      "have the correct title" in new ViewSetup {
        doc.title mustBe s"${page.heading} - Register for VAT - GOV.UK"
      }

      "have the correct link text and href" in new ViewSetup {
        doc.link(1) mustBe Some(Link(page.linkText, page.linkHref))
      }

      "have the link open in a new tab" in new ViewSetup {
        doc.select("form p.govuk-body a").attr("target") mustBe "_blank"
      }

      "replace the old journey hint with the file type inset text" in new ViewSetup {
        doc.panelIndent(1) mustBe Some(ExpectedContent.fileTypeHint)
      }

      "have the correct file upload label" in new ViewSetup {
        doc.select("main label.govuk-label").text() mustBe ExpectedContent.label
      }

      "have the drop zone file upload with the correct text" in new ViewSetup {
        val dropZone = doc.select("main .govuk-drop-zone[data-module=govuk-file-upload]")

        dropZone.size mustBe 1
        dropZone.attr("data-i18n.choose-files-button") mustBe "Choose file"
        dropZone.attr("data-i18n.drop-instruction") mustBe "or drop file"
        dropZone.attr("data-i18n.no-file-chosen") mustBe "No file chosen"
        dropZone.attr("data-i18n.entered-drop-zone") mustBe "Entered drop zone"
        dropZone.attr("data-i18n.left-drop-zone") mustBe "Left drop zone"
      }

      "have the correct submit button" in new ViewSetup {
        doc.submitButton mustBe Some(ExpectedContent.upload)
      }
    }
  }

  "The Upload Documents Page with no error response from upscan" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, Some(Html(ExpectedContent.testHint)), PrimaryIdentityEvidence, None)
    implicit val doc: Document = Jsoup.parse(view.body)
    verifyPageLayout(doc)

    "have no link paragraph" in new ViewSetup {
      doc.select("form p.govuk-body a").size mustBe 0
    }

    "have no drop zone" in new ViewSetup {
      doc.select("main .govuk-drop-zone").size mustBe 0
    }
  }

  "The Upload Documents Page with no error response from upscan for supporting documents" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, None, LandPropertyOtherDocs, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.headingSupporting)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.titleSupporting
    }

    "have no panel text" in new ViewSetup {
      doc.panelIndent(1) mustBe None
    }

    "have no link paragraph" in new ViewSetup {
      doc.select("form p.govuk-body a").size mustBe 0
    }

    "have no drop zone" in new ViewSetup {
      doc.select("main .govuk-drop-zone").size mustBe 0
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The Upload Documents Page for Attachment1614a" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, None, Attachment1614a, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading1614a)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title1614a
    }

    "have no link paragraph" in new ViewSetup {
      doc.select("form p.govuk-body a").size mustBe 0
    }

    "have no drop zone" in new ViewSetup {
      doc.select("main .govuk-drop-zone").size mustBe 0
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The Upload Documents Page with error response from upscan" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, Some(Html(ExpectedContent.testHint)), PrimaryIdentityEvidence, Some("EntityTooLarge"))
    implicit val doc: Document = Jsoup.parse(view.body)

    verifyPageLayout(doc)

    "have a correct error summary" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.fileUploadError
    }

    "have a correct error summary link" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.fileUploadError.stripPrefix("Error: "), "#file-upload-1"))
    }
  }

  "The Upload Documents Page with an unrecognised error code from upscan" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, Some(Html(ExpectedContent.testHint)), PrimaryIdentityEvidence, Some("InternalError"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the generic error message" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.genericFileUploadError
    }

    "have a correct error summary link to the file upload field" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.genericFileUploadError.stripPrefix("Error: "), "#file-upload-1"))
    }
  }

  formPages.foreach { page =>

    s"The Upload Documents Page in Welsh for ${page.attachmentType}" must {
      lazy val view: Html = uploadDocumentsPage(
        testUpscanResponse,
        Some(Html(ExpectedContent.testHint)),
        page.attachmentType,
        None
      )(request, welshMessages, appConfig)
      implicit val doc: Document = Jsoup.parse(view.body)

      "have the correct Welsh heading" in new ViewSetup {
        doc.heading mustBe Some(page.welshHeading)
      }

      "have the correct Welsh link text" in new ViewSetup {
        doc.link(1).map(_.text) mustBe Some(page.welshLinkText)
      }

      "replace the old journey hint with the Welsh file type inset text" in new ViewSetup {
        doc.panelIndent(1) mustBe Some("Mae’n rhaid i’r ffeil fod ar ffurf JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF neu TXT.")
      }

      "have the correct Welsh label" in new ViewSetup {
        doc.select("main label.govuk-label").text() mustBe "Uwchlwytho ffeil"
      }

      "have the Welsh drop zone text from the HMRC design system file upload pattern" in new ViewSetup {
        val dropZone = doc.select("main .govuk-drop-zone")

        dropZone.attr("data-i18n.choose-files-button") mustBe "Dewis ffeil"
        dropZone.attr("data-i18n.drop-instruction") mustBe "neu ollwng ffeil"
        dropZone.attr("data-i18n.no-file-chosen") mustBe "Dim ffeil wedi’i dewis"
        dropZone.attr("data-i18n.entered-drop-zone") mustBe "Yn y man gollwng"
        dropZone.attr("data-i18n.left-drop-zone") mustBe "Wedi gadael y man gollwng"
      }

      "have the correct Welsh submit button" in new ViewSetup {
        doc.submitButton mustBe Some("Uwchlwytho")
      }
    }
  }

  private def verifyPageLayout(implicit doc: Document): Unit = {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct panel text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.testHint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
