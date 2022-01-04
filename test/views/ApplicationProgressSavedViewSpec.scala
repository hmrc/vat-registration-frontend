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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.pages.application_progress_saved

class ApplicationProgressSavedViewSpec extends VatRegViewSpec {

  val heading = "Your application has been saved for 7 days"
  val title = s"$heading - Register for VAT - GOV.UK"
  val paragraph = "You will not be able to submit the application until you have completed the application."
  val paragraph2 = "You can leave this page, or "
  val linkText = "return to your application"
  val insetText = "Saved applications will only be available for 7 days"

  val view: application_progress_saved = app.injector.instanceOf[application_progress_saved]

  "Application submission confirmation page" should {
    implicit val doc: Document = Jsoup.parse(view().body)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(paragraph)
      doc.para(2) mustBe Some(paragraph2 + linkText)
    }

    "have the correct Link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(linkText, "#"))
    }

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct inset text" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe insetText
    }
  }
}