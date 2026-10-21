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

import models.external.upscan.UpscanResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.UploadIdentityEvidence

import scala.jdk.CollectionConverters._

class UploadIdentityEvidenceViewSpec extends VatRegViewSpec {

  val uploadIdentityEvidencePage: UploadIdentityEvidence = app.injector.instanceOf[UploadIdentityEvidence]
  val testReference = "testReference"
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  object ExpectedContent {
    val testName = "Test Name"
    val heading = s"Upload $testName’s identity evidence"
    val title = s"$heading - Register for VAT - GOV.UK"
    val fallbackHeading = "Upload a document"
    val fallbackTitle = s"$fallbackHeading - Register for VAT - GOV.UK"
    val para1 = "You need to upload 2 of the following:"
    val bullets = List(
      "your birth certificate",
      "a lease or rental agreement",
      "your work permit or visa",
      "a recent utility bill",
      "a recent mortgage statement",
      "a recent bank statement",
      "a recent credit card statement",
      "any documents from the Department for Work and Pensions that confirms your entitlement to benefits"
    )
    val para2 = "Your documents should be dated within the last 3 months, where possible."
    val fileTypeHint = "The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val zeroUploaded = "0 of 2 files uploaded"
    val oneUploaded = "1 of 2 files uploaded"
    val labelFirst = "First item of evidence"
    val labelSecond = "Second item of evidence"
    val continue = "Continue"
    val fileUploadError = "Error: The selected file must be smaller than 10MB"
    val genericFileUploadError = "Error: The selected file could not be uploaded"
  }

  "The Upload Identity Evidence Page with a name and no files uploaded" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 0, None)
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

    "have the correct introduction" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
    }

    "embolden only the number of files to upload and the uploaded count" in new ViewSetup {
      doc.select("main p strong").eachText().asScala.toList mustBe List("upload 2", ExpectedContent.zeroUploaded)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe ExpectedContent.bullets
    }

    "have the correct date guidance" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the file type inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedContent.fileTypeHint)
    }

    "have the uploaded count in a polite live region" in new ViewSetup {
      doc.select("p[aria-live=polite] strong").text() mustBe ExpectedContent.zeroUploaded
    }

    "have the label for the first item of evidence" in new ViewSetup {
      doc.select("main label.govuk-label").text() mustBe ExpectedContent.labelFirst
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

    "have a single file input that upscan can read, limited to the accepted file types" in new ViewSetup {
      val fileInputs = doc.select("main input[type=file]")

      fileInputs.size mustBe 1
      fileInputs.attr("id") mustBe "file-upload-1"
      fileInputs.attr("name") mustBe "file"
      fileInputs.attr("accept") mustBe ".pdf,.doc,.docx,.xls,.xlsx,.bmp,.gif,.png,.jpeg,.jpg,.txt"
    }

    "have the correct submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }

    "include the upscan fields as hidden inputs" in new ViewSetup {
      doc.select("input[type=hidden][name=testField1]").attr("value") mustBe "test1"
      doc.select("input[type=hidden][name=testField2]").attr("value") mustBe "test2"
    }

    "post the form to the upscan href as multipart" in new ViewSetup {
      doc.select("form").attr("action") mustBe testHref
      doc.select("form").attr("enctype") mustBe "multipart/form-data"
    }

    "not have an error summary" in new ViewSetup {
      doc.hasErrorSummary mustBe false
    }
  }

  "The Upload Identity Evidence Page with one file already uploaded" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 1, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "show one of two files uploaded" in new ViewSetup {
      doc.select("p[aria-live=polite] strong").text() mustBe ExpectedContent.oneUploaded
    }

    "have the label for the second item of evidence" in new ViewSetup {
      doc.select("main label.govuk-label").text() mustBe ExpectedContent.labelSecond
    }
  }

  "The Upload Identity Evidence Page without a name" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, None, 0, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "fall back to the generic heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.fallbackHeading)
    }

    "fall back to the generic title" in new ViewSetup {
      doc.title mustBe ExpectedContent.fallbackTitle
    }
  }

  "The Upload Identity Evidence Page with a name containing HTML" must {
    val unsafeName = "<script>alert(1)</script> O’Neil"
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(unsafeName), 0, None)
    implicit val doc: Document = Jsoup.parse(view.body)

    "show the name as text in the heading" in new ViewSetup {
      doc.heading mustBe Some(s"Upload $unsafeName’s identity evidence")
    }

    "not render the name as markup anywhere on the page" in new ViewSetup {
      doc.select("h1 script").size mustBe 0
      doc.select("title script").size mustBe 0
      view.body must not include "<script>alert(1)</script>"
    }
  }

  "The Upload Identity Evidence Page with error response from upscan" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 0, Some("EntityTooLarge"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a correct error message" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.fileUploadError
    }

    "have a correct error summary link" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.fileUploadError.stripPrefix("Error: "), "#file-upload-1"))
    }
  }

  "The Upload Identity Evidence Page with an unrecognised error code from upscan" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 0, Some("InternalError"))
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the generic error message" in new ViewSetup {
      doc.select(".govuk-error-message").text() mustBe ExpectedContent.genericFileUploadError
    }

    "have a correct error summary link to the file upload field" in new ViewSetup {
      doc.errorSummaryLinks mustBe List(Link(ExpectedContent.genericFileUploadError.stripPrefix("Error: "), "#file-upload-1"))
    }
  }

  "The Upload Identity Evidence Page in Welsh with one file already uploaded" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 1, None)(request, welshMessages, appConfig)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the Welsh uploaded count and label for the second item of evidence" in new ViewSetup {
      doc.select("p[aria-live=polite] strong").text() mustBe "1 o 2 ffeil wedi’u huwchlwytho"
      doc.select("main label.govuk-label").text() mustBe "Ail eitem o dystiolaeth"
    }
  }

  "The Upload Identity Evidence Page in Welsh" must {
    lazy val view: Html = uploadIdentityEvidencePage(testUpscanResponse, Some(ExpectedContent.testName), 0, None)(request, welshMessages, appConfig)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the Welsh heading and title with the name" in new ViewSetup {
      doc.heading mustBe Some(s"Uwchlwytho tystiolaeth sy’n profi pwy yw ${ExpectedContent.testName}")
      doc.title mustBe s"Uwchlwytho tystiolaeth sy’n profi pwy yw ${ExpectedContent.testName} - Cofrestru ar gyfer TAW - GOV.UK"
    }

    "have the Welsh introduction" in new ViewSetup {
      doc.para(1) mustBe Some("Mae angen i chi uwchlwytho 2 o’r canlynol:")
    }

    "have the Welsh uploaded count and label for the first item of evidence" in new ViewSetup {
      doc.select("p[aria-live=polite] strong").text() mustBe "0 o 2 ffeil wedi’u huwchlwytho"
      doc.select("main label.govuk-label").text() mustBe "Eitem gyntaf o dystiolaeth"
    }

    "have the Welsh bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        "eich tystysgrif geni",
        "prydles neu gytundeb rhentu",
        "eich trwydded waith neu’ch fisa",
        "bil cyfleustodau diweddar",
        "datganiad morgais diweddar",
        "cyfriflen banc ddiweddar",
        "datganiad cerdyn credyd diweddar",
        "unrhyw ddogfennau gan yr Adran Gwaith a Phensiynau sy’n cadarnhau’ch hawl i fudd-daliadau"
      )
    }

    "have the Welsh date guidance" in new ViewSetup {
      doc.para(2) mustBe Some("Dylai’ch dogfennau fod wedi’u dyddio o fewn y 3 mis diwethaf, lle bo hynny’n bosibl.")
    }

    "have the Welsh file type inset text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some("Mae’n rhaid i’r ffeil fod ar ffurf JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF neu TXT.")
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
      doc.submitButton mustBe Some("Yn eich blaen")
    }
  }
}
