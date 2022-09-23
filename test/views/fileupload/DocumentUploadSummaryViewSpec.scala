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

package views.fileupload

import fixtures.VatRegistrationFixture
import forms.DocumentUploadSummaryForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import viewmodels.DocumentUploadSummaryRow
import views.VatRegViewSpec
import views.html.fileupload.DocumentUploadSummary

class DocumentUploadSummaryViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[DocumentUploadSummary]

  val testReference = "reference"
  val testDocument = "test-document"

  object ExpectedMessages {
    val heading = "You have added 1 document"
    val removeLink = "Remove"
    val submitButton = "Continue"
  }

  val removeLink: Call = controllers.fileupload.routes.RemoveUploadedDocumentController.submit(testReference)

  val testList = List(DocumentUploadSummaryRow(testDocument, removeLink))

  "The document upload summary view" must {
    implicit val doc: Document = Jsoup.parse(view(DocumentUploadSummaryForm.form, testList, testList.size, supplySupportingDocuments = false).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a single uploaded document" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 1
    }
    "have correct table heading" in new ViewSetup {
      doc.select("th").text() mustBe testDocument
    }
    "have a remove link with the correct URL" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.removeLink, removeLink.url))
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.submitButton)
    }
  }

  "The document upload summary view with supporting documents" must {
    implicit val doc: Document = Jsoup.parse(view(DocumentUploadSummaryForm.form, testList, 0, supplySupportingDocuments = true).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a single uploaded document" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 1
    }
    "have a remove link with the correct URL" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.removeLink, removeLink.url))
    }
    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some("Yes")
      doc.radio("false") mustBe Some("No")
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.submitButton)
    }
  }
}
