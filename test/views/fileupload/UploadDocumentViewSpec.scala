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


import models.api.{LandPropertyOtherDocs, PrimaryIdentityEvidence}
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
    val headingSupporting = "Upload a supporting document"
    val title = s"$heading - Register for VAT - GOV.UK"
    val titleSupporting = s"$headingSupporting - Register for VAT - GOV.UK"
    val testHint = "testHint"
    val label = "Upload a file"
    val continue = "Continue"
    val fileUploadError = "Error: The selected file must be smaller than 10MB"
  }

  "The Upload Documents Page with no error response from upscan" must {
    lazy val view: Html = uploadDocumentsPage(testUpscanResponse, Some(Html(ExpectedContent.testHint)), PrimaryIdentityEvidence, None)
    verifyPageLayout(Jsoup.parse(view.body))
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