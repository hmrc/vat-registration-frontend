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

import fixtures.VatRegistrationFixture
import models.api.{Attachment1614a, LandPropertyOtherDocs, VAT5L}
import models.external.upscan.{Failed, InProgress, Ready}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import viewmodels.UploadedVat5LDocumentRow
import views.VatRegViewSpec
import views.html.fileupload.UploadedVat5LDocuments

import scala.jdk.CollectionConverters._

class UploadedVat5LDocumentsViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view: UploadedVat5LDocuments = app.injector.instanceOf[UploadedVat5LDocuments]

  val testReference = "reference"
  val testDocument = "test-document"

  object ExpectedMessages {
    val heading = "Uploaded VAT5L documents"
    val tableHeadings = List("Document type", "File name", "Status", "Actions")
    val removeLink = "Remove"
    val submitButton = "Continue"
  }

  val removeLink: Call = controllers.fileupload.routes.RemoveVat5LDocumentController.show(testReference)

  val testList: List[UploadedVat5LDocumentRow] = List(
    UploadedVat5LDocumentRow(VAT5L, Some(testDocument), Ready, Some(removeLink)),
    UploadedVat5LDocumentRow(LandPropertyOtherDocs, None, InProgress, None),
    UploadedVat5LDocumentRow(Attachment1614a, None, Failed, None)
  )

  "The uploaded VAT5L documents view" must {
    implicit val doc: Document = Jsoup.parse(view(testList, 0).body)

    def row(index: Int): List[String] = doc.select("tbody tr").get(index).children().asScala.map(_.text).toList

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "show both paragraphs inside the details component" in new ViewSetup {
      doc.select(".govuk-details p").size mustBe 2
    }
    "have the table column headings" in new ViewSetup {
      doc.select("thead th").asScala.map(_.text).toList mustBe ExpectedMessages.tableHeadings
    }
    "have the actions column heading visually hidden" in new ViewSetup {
      doc.select("thead th").last.select(".govuk-visually-hidden").text mustBe "Actions"
    }
    "have a row for each document" in new ViewSetup {
      doc.select("tbody tr").size mustBe 3
    }
    "show an uploaded document with its type, file name, status and remove link" in new ViewSetup {
      row(0) mustBe List("VAT5L", testDocument, "Uploaded", ExpectedMessages.removeLink)
      doc.select("tbody tr").get(0).select("a").attr("href") mustBe removeLink.url
    }
    "show a document that is still uploading without a file name or remove link" in new ViewSetup {
      row(1) mustBe List("Supporting document", "", "Uploading", "")
      doc.select("tbody tr").get(1).select("a").size mustBe 0
    }
    "show a failed document without a file name or remove link" in new ViewSetup {
      row(2) mustBe List("VAT1614A", "", "Failed", "")
      doc.select("tbody tr").get(2).select("a").size mustBe 0
    }
    "use a different tag colour for each status" in new ViewSetup {
      doc.select("tbody tr").get(0).select(".govuk-tag").hasClass("govuk-tag--green") mustBe true
      doc.select("tbody tr").get(1).select(".govuk-tag").hasClass("govuk-tag--blue") mustBe true
      doc.select("tbody tr").get(2).select(".govuk-tag").hasClass("govuk-tag--red") mustBe true
    }
    "have a button to upload more supporting documents" in new ViewSetup {
      val uploadButton = doc.select("a.govuk-button--secondary")
      uploadButton.text mustBe "Upload supporting document"
      uploadButton.attr("href") mustBe controllers.fileupload.routes.SupportingDocumentUploadController.show.url
    }
    "not have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe None
      doc.radio("false") mustBe None
    }
    "have a submit button that posts to continue" in new ViewSetup {
      val continueForm = doc.select("form")
      continueForm.attr("action") mustBe controllers.fileupload.routes.UploadedVat5LDocumentsController.continue.url
      continueForm.select("button").text mustBe ExpectedMessages.submitButton
    }
  }
}
