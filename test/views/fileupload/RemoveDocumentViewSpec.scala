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

import models.api.{AttachmentType, TaxRepresentativeAuthorisation, VAT2, VAT51, VAT5L}
import models.external.upscan.{Failed, FileStatus, InProgress, Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.fileupload.RemoveDocument

import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

class RemoveDocumentViewSpec extends VatRegViewSpec {

  val view: RemoveDocument = app.injector.instanceOf[RemoveDocument]
  val testReference = "test-reference"
  val testFileName = "partnership-details.pdf"

  def upscanDetails(attachmentType: AttachmentType, fileStatus: FileStatus, fileName: Option[String] = Some(testFileName)): UpscanDetails =
    UpscanDetails(
      attachmentType = attachmentType,
      reference = testReference,
      fileStatus = fileStatus,
      uploadDetails = fileName.map(UploadDetails(_, "application/pdf", LocalDateTime.of(2026, 10, 15, 9, 0), "checksum", 100))
    )

  def render(details: UpscanDetails): Document = Jsoup.parse(view(details).body)

  def tableHeaders(doc: Document): List[String] = doc.select("main table thead th").eachText().asScala.toList
  def tableRow(doc: Document): List[String] = doc.select("main table tbody tr").first().children().asScala.toList.map(_.text)

  Seq(
    VAT2 -> "VAT2",
    VAT51 -> "VAT50/51",
    TaxRepresentativeAuthorisation -> "VAT1TR",
    VAT5L -> "VAT5L"
  ).foreach { case (attachmentType, documentType) =>
    s"The Remove document page for $attachmentType" must {
      implicit val doc: Document = render(upscanDetails(attachmentType, Ready))

      "name the form in the heading and title" in new ViewSetup {
        doc.heading mustBe Some(s"Remove $documentType document")
        doc.title mustBe s"Remove $documentType document - Register for VAT - GOV.UK"
      }

      "show the form, file name and status in the table" in new ViewSetup {
        tableRow(doc) mustBe List(documentType, testFileName, "Uploaded")
      }
    }
  }

  "The Remove document page" must {
    implicit val doc: Document = render(upscanDetails(VAT2, Ready))

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "use the full page width for the table, not the default two-thirds column" in new ViewSetup {
      doc.select("main .govuk-grid-column-two-thirds").size mustBe 0
    }

    "label the table columns" in new ViewSetup {
      tableHeaders(doc) mustBe List("Document type", "File name", "Status")
    }

    "mark the document type cell as the row header" in new ViewSetup {
      doc.select("main table tbody tr th").text() mustBe "VAT2"
    }

    "ask whether to remove the document" in new ViewSetup {
      doc.select("main h2").text() mustBe "Do you want to remove this document?"
    }

    "post both choices to the remove URL for this document" in new ViewSetup {
      val form = doc.select("main form")

      form.attr("action") mustBe s"/register-for-vat/file-upload/$testReference/remove"
      form.attr("method") mustBe "POST"
    }

    "submit remove from a warning button and go back from a secondary button" in new ViewSetup {
      val remove = doc.select("main form button#remove-button")
      val goBack = doc.select("main form button#go-back-button")

      remove.text() mustBe "Remove this document"
      remove.attr("name") mustBe "choice"
      remove.attr("value") mustBe "remove"
      remove.hasClass("govuk-button--warning") mustBe true

      goBack.text() mustBe "Go back"
      goBack.attr("name") mustBe "choice"
      goBack.attr("value") mustBe "cancel"
      goBack.hasClass("govuk-button--secondary") mustBe true
    }
  }

  Seq(
    (InProgress, "Uploading", "govuk-tag--yellow"),
    (Ready, "Uploaded", "govuk-tag--green"),
    (Failed, "Not uploaded", "govuk-tag--red")
  ).foreach { case (fileStatus, label, tagClass) =>
    s"The Remove document page for a $fileStatus upload" must {
      implicit val doc: Document = render(upscanDetails(VAT2, fileStatus))

      s"show a '$label' tag coloured with $tagClass" in new ViewSetup {
        val tag = doc.select("main table tbody .govuk-tag")

        tag.text() mustBe label
        tag.hasClass(tagClass) mustBe true
      }
    }
  }

  "The Remove document page for an upload with no file details yet" must {
    implicit val doc: Document = render(upscanDetails(VAT2, InProgress, fileName = None))

    "leave the file name cell empty" in new ViewSetup {
      tableRow(doc) mustBe List("VAT2", "", "Uploading")
    }
  }

  "The Remove document page for a file name containing HTML" must {
    val unsafeFileName = "<script>alert(1)</script>.pdf"
    implicit val doc: Document = render(upscanDetails(VAT2, Ready, fileName = Some(unsafeFileName)))

    "show the file name as text" in new ViewSetup {
      tableRow(doc) mustBe List("VAT2", unsafeFileName, "Uploaded")
      doc.select("main script").size mustBe 0
    }
  }

  "The Remove document page in Welsh" must {
    implicit val doc: Document = Jsoup.parse(view(upscanDetails(VAT51, Ready))(request, welshMessages, appConfig).body)

    "keep the form's code as the document type" in new ViewSetup {
      doc.select("main table tbody tr th").text() mustBe "VAT50/51"
    }
  }
}
