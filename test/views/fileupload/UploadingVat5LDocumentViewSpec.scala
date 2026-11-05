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

import models.api.VAT5L
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.UploadingVat5LDocument

import java.time.LocalDateTime

class UploadingVat5LDocumentViewSpec extends VatRegViewSpec {

  val uploadingDocumentPage: UploadingVat5LDocument = app.injector.instanceOf[UploadingVat5LDocument]

  lazy val view: Html = uploadingDocumentPage(UpscanDetails(
    attachmentType = VAT5L,
    reference = "testReference",
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  ))
  implicit val doc: Document = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "Uploading the VAT5L"
    val title = s"$heading - Register for VAT - GOV.UK"
    val subheading = "We are checking your file."
    val continue = "Save and continue"
  }

  "The Uploading Document page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.getElementsByTag("title").first().text() mustBe ExpectedContent.title
    }

    "have the correct subheading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.subheading)
    }
  }

}
