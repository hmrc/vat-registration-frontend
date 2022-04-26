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


import models.external.upscan.UpscanResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.UploadDocument

class UploadDocumentViewSpec extends VatRegViewSpec {

  val uploadDocumentsPage: UploadDocument = app.injector.instanceOf[UploadDocument]
  val testReference = "testReference"
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  object ExpectedContent {
    val heading = "Upload a document"
    val title = s"$heading - Register for VAT - GOV.UK"
    val testHint = "testHint"
    val label = "Upload a file"
    val continue = "Save and continue"
  }

  "The Upload Documents Page" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, Html(ExpectedContent.testHint))
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

    "have the correct panel text" in new ViewSetup {
      doc.panelIndent(0) mustBe Some(ExpectedContent.testHint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
