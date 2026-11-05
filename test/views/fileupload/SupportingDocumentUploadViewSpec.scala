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

import models.external.upscan.UpscanResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.SupportingDocumentUpload

class SupportingDocumentUploadViewSpec extends VatRegViewSpec {

  val uploadPage: SupportingDocumentUpload = app.injector.instanceOf[SupportingDocumentUpload]
  val testReference = "testReference"
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  object ExpectedContent {
    val heading = "Upload a supporting document"
    val title = s"$heading - Register for VAT - GOV.UK"
    val fileTypeHint = "The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val label = "Upload a file"
    val upload = "Upload"
    val fileUploadError = "Error: The selected file must be smaller than 10MB"
    val genericFileUploadError = "Error: The selected file could not be uploaded"
  }

  "The supporting document upload page" must {
    lazy val view: Html = uploadPage(testUpscanResponse, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the file type inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.fileTypeHint)
    }

    "not have a list of uploaded documents" in new ViewSetup {
      doc.select(".govuk-summary-list").size mustBe 0
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

    "post the upload form to upscan with the upscan fields" in new ViewSetup {
      val uploadForm = doc.select("form[enctype=multipart/form-data]")
      uploadForm.attr("action") mustBe testHref
      uploadForm.select("input[name=testField1]").attr("value") mustBe "test1"
      uploadForm.select("input[name=testField2]").attr("value") mustBe "test2"
    }

    "have a primary upload button" in new ViewSetup {
      doc.select("#file-upload-button").text mustBe ExpectedContent.upload
      doc.select("#file-upload-button").hasClass("govuk-button--secondary") mustBe false
    }

    "not have a continue button" in new ViewSetup {
      doc.select("main form").size mustBe 1
    }
  }

  "The supporting document upload page with error response from upscan" must {
    lazy val view: Html = uploadPage(testUpscanResponse, Some("EntityTooLarge"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a correct error message" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.fileUploadError
    }

    "have a correct error summary link" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.fileUploadError.stripPrefix("Error: "), "#file-upload-1"))
    }
  }

  "The supporting document upload page with an unknown error response from upscan" must {
    lazy val view: Html = uploadPage(testUpscanResponse, Some("Unknown"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "fall back to the generic error message" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.genericFileUploadError
    }
  }
}
