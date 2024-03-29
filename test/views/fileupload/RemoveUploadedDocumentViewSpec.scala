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
import forms.RemoveUploadedDocumentForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import viewmodels.DocumentUploadSummaryRow
import views.VatRegViewSpec
import views.html.fileupload.RemoveUploadedDocument

class RemoveUploadedDocumentViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val testReference = "reference"
  val testDocumentName = "testDocumentName"

  object ExpectedMessages {
    val heading = s"Are you sure you want to remove $testDocumentName?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val continue = "Save and continue"
  }

  val removeLink: Call = controllers.fileupload.routes.RemoveUploadedDocumentController.submit(testReference)
  val testList = List(DocumentUploadSummaryRow("test-document", removeLink))

  val view: RemoveUploadedDocument = app.injector.instanceOf[RemoveUploadedDocument]
  implicit val doc: Document = Jsoup.parse(view(RemoveUploadedDocumentForm(testDocumentName).form, testReference, testDocumentName).body)

  "Remove uploaded document page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedMessages.title
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.continue)
    }
  }

}
