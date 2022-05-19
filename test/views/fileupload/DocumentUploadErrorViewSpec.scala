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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.fileupload.UploadDocumentError

class DocumentUploadErrorViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view: UploadDocumentError = app.injector.instanceOf[UploadDocumentError]

  object ExpectedMessages {
    val heading = "There is a problem with the selected file"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button = "Go back and upload a new file"
  }

  implicit val doc: Document = Jsoup.parse(view().body)

  "The document upload summary view" must {
    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedMessages.title
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a link button with the correct URL" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.button, controllers.fileupload.routes.UploadDocumentController.show.url))
    }
  }

}
