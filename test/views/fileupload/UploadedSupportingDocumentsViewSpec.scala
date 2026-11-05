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

import forms.UploadedSupportingDocumentsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import viewmodels.DocumentUploadSummaryRow
import views.VatRegViewSpec
import views.html.fileupload.UploadedSupportingDocuments

import scala.jdk.CollectionConverters._

class UploadedSupportingDocumentsViewSpec extends VatRegViewSpec {

  val page: UploadedSupportingDocuments = app.injector.instanceOf[UploadedSupportingDocuments]
  val removeLink: Call = controllers.fileupload.routes.RemoveSupportingDocumentController.show("testReference")
  val documents: Seq[DocumentUploadSummaryRow] = Seq(
    DocumentUploadSummaryRow("planning.pdf", removeLink),
    DocumentUploadSummaryRow("letter.docx", removeLink)
  )

  object ExpectedContent {
    val heading = "You have added 2 files"
    val headingSingular = "You have added 1 file"
    val title = s"$heading - Register for VAT - GOV.UK"
    val legend = "Do you need to upload another file?"
    val limitReached = "You have uploaded the maximum of 10 supporting documents."
    val error = "Select yes if you need to upload another file"
    val continue = "Continue"
  }

  "The uploaded supporting documents page" must {
    implicit val doc: Document = Jsoup.parse(page(UploadedSupportingDocumentsForm.form, documents, canAddAnother = true).body)

    "have the correct heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
      doc.title mustBe ExpectedContent.title
    }

    "list each document with its status and a remove link" in new ViewSetup {
      val rows = doc.select(".govuk-summary-list__row").asScala.toList
      rows.map(_.select(".govuk-summary-list__key").text) mustBe List("planning.pdf", "letter.docx")
      rows.map(_.select(".govuk-tag").text) mustBe List("Uploaded", "Uploaded")
      rows.map(_.select(".govuk-summary-list__actions a").attr("href")) mustBe List(removeLink.url, removeLink.url)
    }

    "not show the file names in bold" in new ViewSetup {
      doc.select(".govuk-summary-list__key").asScala.toList.map(_.hasClass("govuk-!-font-weight-regular")) mustBe List(true, true)
    }

    "include the file name in the remove link for screen readers" in new ViewSetup {
      doc.select(".govuk-summary-list__actions a").first.select(".govuk-visually-hidden").text mustBe "planning.pdf"
    }

    "ask whether to add another file" in new ViewSetup {
      doc.legend(1) mustBe Some(ExpectedContent.legend)
      doc.radio("true") mustBe Some("Yes")
      doc.radio("false") mustBe Some("No")
    }

    "post to the UploadedSupportingDocumentsController" in new ViewSetup {
      doc.select("main form").attr("action") mustBe controllers.fileupload.routes.UploadedSupportingDocumentsController.submit.url
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The uploaded supporting documents page with a single document" must {
    implicit val doc: Document = Jsoup.parse(page(UploadedSupportingDocumentsForm.form, documents.take(1), canAddAnother = true).body)

    "have the singular heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.headingSingular)
      doc.title mustBe s"${ExpectedContent.headingSingular} - Register for VAT - GOV.UK"
    }
  }

  "The uploaded supporting documents page at the file limit" must {
    implicit val doc: Document = Jsoup.parse(page(UploadedSupportingDocumentsForm.form, documents, canAddAnother = false).body)

    "not ask whether to add another file" in new ViewSetup {
      doc.select("input[type=radio]").size mustBe 0
    }

    "explain that the limit has been reached and keep the continue button" in new ViewSetup {
      doc.select("main form p").text mustBe ExpectedContent.limitReached
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

  "The uploaded supporting documents page with no answer selected" must {
    implicit val doc: Document = Jsoup.parse(page(UploadedSupportingDocumentsForm.form.bind(Map.empty[String, String]), documents, canAddAnother = true).body)

    "show the error" in new ViewSetup {
      doc.errorSummaryLinks.map(_.text) mustBe List(ExpectedContent.error)
    }
  }
}
