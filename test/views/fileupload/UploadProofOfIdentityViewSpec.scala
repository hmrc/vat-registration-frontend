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

package views.fileupload

import models.PersonalDetails
import models.external.upscan.UpscanResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.UploadProofOfIdentity

class UploadProofOfIdentityViewSpec extends VatRegViewSpec {

  val uploadProofOfIdentityPage: UploadProofOfIdentity = app.injector.instanceOf[UploadProofOfIdentity]
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse("testReference", testHref, Map("testField1" -> "test1", "testField2" -> "test2"))
  val testPerson: PersonalDetails = PersonalDetails("Joanne", "Bloggs", identifiersMatch = false)

  object ExpectedContent {
    val title = "Upload proof of identity - Register for VAT - GOV.UK"
    val heading = "Upload Joanne Bloggs’s proof of identity"
    val fallbackHeading = "Upload proof of identity"
    val para = "This must be a copy of either Joanne Bloggs’s passport, driving licence photocard or national identity card."
    val fileTypeHint = "The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val label = "Upload a file"
    val upload = "Upload"
    val fileUploadError = "The selected file must be smaller than 10MB"
    val genericFileUploadError = "The selected file could not be uploaded"
  }

  "The Upload Proof of Identity Page for a named person" must {
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, Some(testPerson), None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the full name in the heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "keep the name out of the browser title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "describe the accepted documents using the full name" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the file type inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.fileTypeHint)
    }

    "have a single labelled file input that upscan can read, limited to the accepted file types" in new ViewSetup {
      val fileInputs = doc.select("main input[type=file]")

      fileInputs.size mustBe 1
      fileInputs.attr("id") mustBe "file-upload-1"
      fileInputs.attr("name") mustBe "file"
      fileInputs.attr("accept") mustBe ".pdf,.doc,.docx,.xls,.xlsx,.bmp,.gif,.png,.jpeg,.jpg,.txt"
      doc.select("main label[for=file-upload-1]").text() mustBe ExpectedContent.label
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

    "have the upload submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.upload)
    }

    "post the upscan fields to the upscan href as multipart" in new ViewSetup {
      doc.select("form").attr("action") mustBe testHref
      doc.select("form").attr("enctype") mustBe "multipart/form-data"
      doc.select("input[type=hidden][name=testField1]").attr("value") mustBe "test1"
      doc.select("input[type=hidden][name=testField2]").attr("value") mustBe "test2"
    }

    "not have an error summary" in new ViewSetup {
      doc.hasErrorSummary mustBe false
    }
  }

  "The Upload Proof of Identity Page without personal details" must {
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, None, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "use the nameless heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.fallbackHeading)
    }

    "leave out the sentence that needs a name but keep the file type inset text" in new ViewSetup {
      doc.paras must not contain ExpectedContent.para
      doc.select("main form > p").size mustBe 0
      doc.panelIndent(1) mustBe Some(ExpectedContent.fileTypeHint)
    }
  }

  "The Upload Proof of Identity Page with a name containing HTML" must {
    val unsafePerson = testPerson.copy(firstName = "<script>alert(1)</script>", lastName = "O’Neil")
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, Some(unsafePerson), None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "show the name as text in the heading and the body" in new ViewSetup {
      doc.heading mustBe Some("Upload <script>alert(1)</script> O’Neil’s proof of identity")
      doc.para(1) mustBe Some("This must be a copy of either <script>alert(1)</script> O’Neil’s passport, driving licence photocard or national identity card.")
    }

    "not render the name as markup anywhere on the page" in new ViewSetup {
      doc.select("main script").size mustBe 0
      view.body must not include "<script>alert(1)</script>"
    }
  }

  "The Upload Proof of Identity Page with error response from upscan" must {
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, Some(testPerson), Some("EntityTooLarge"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the error against the file input" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe s"Error: ${ExpectedContent.fileUploadError}"
    }

    "link the error summary to the file input" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.fileUploadError, "#file-upload-1"))
    }
  }

  "The Upload Proof of Identity Page with an unrecognised error code from upscan" must {
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, Some(testPerson), Some("InternalError"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "fall back to the generic error in the message and the summary" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe s"Error: ${ExpectedContent.genericFileUploadError}"
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.genericFileUploadError, "#file-upload-1"))
    }
  }

  "The Upload Proof of Identity Page in Welsh" must {
    lazy val view: Html = uploadProofOfIdentityPage(testUpscanResponse, Some(testPerson), None)(request, welshMessages, appConfig)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the Welsh browser title without the name" in new ViewSetup {
      doc.title mustBe "Uwchlwytho prawf o hunaniaeth - Cofrestru ar gyfer TAW - GOV.UK"
    }

    "have the Welsh heading with the full name" in new ViewSetup {
      doc.heading mustBe Some("Uwchlwytho prawf o bwy yw Joanne Bloggs")
    }

    "have the Welsh description of the accepted documents with the full name" in new ViewSetup {
      doc.para(1) mustBe Some("Mae’n rhaid i’r dystiolaeth hon fod yn gopi o naill ai basbort, trwydded yrru cerdyn-llun, neu gerdyn adnabod cenedlaethol Joanne Bloggs.")
    }

    "have the Welsh file type inset text, label and submit button" in new ViewSetup {
      doc.panelIndent(1) mustBe Some("Mae’n rhaid i’r ffeil fod ar ffurf JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF neu TXT.")
      doc.select("main label[for=file-upload-1]").text() mustBe "Uwchlwytho ffeil"
      doc.submitButton mustBe Some("Uwchlwytho")
    }

    "have the Welsh drop zone text from the HMRC design system file upload pattern" in new ViewSetup {
      val dropZone = doc.select("main .govuk-drop-zone")

      dropZone.attr("data-i18n.choose-files-button") mustBe "Dewis ffeil"
      dropZone.attr("data-i18n.drop-instruction") mustBe "neu ollwng ffeil"
      dropZone.attr("data-i18n.no-file-chosen") mustBe "Dim ffeil wedi’i dewis"
      dropZone.attr("data-i18n.entered-drop-zone") mustBe "Yn y man gollwng"
      dropZone.attr("data-i18n.left-drop-zone") mustBe "Wedi gadael y man gollwng"
    }
  }
}
