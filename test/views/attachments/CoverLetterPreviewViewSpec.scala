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

package views.attachments

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.CoverLetterPreview

class CoverLetterPreviewViewSpec extends VatRegViewSpec {

  val view: CoverLetterPreview = app.injector.instanceOf[CoverLetterPreview]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "Cover letter"
    val title = s"$heading - Register for VAT - GOV.UK"
    val p1 = "You now need to print or download the VAT registration cover letter, so you can post it with your documents."
    val p2 = "It’s important because it allows us to match up your online application with your posted documents."
    val button = "Get cover letter"
  }

  "The cover letter preview page" must {
    "should not have a back link" in new ViewSetup {
      doc.hasBackLink mustBe false
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
      doc.para(2) mustBe Some(ExpectedContent.p2)
    }

    "have a Get cover letter button linking to the application submission page" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.button)
      doc.select("a.govuk-button").attr("href") mustBe controllers.routes.ApplicationSubmissionController.show.url
    }
  }
}
