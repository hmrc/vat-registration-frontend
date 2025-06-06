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

package views

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.SubmitDeadline
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ApplicationProgressSaved

class ApplicationProgressSavedViewSpec extends VatRegViewSpec {

  val heading = "Your application has been saved for 7 days"
  val ttDeadlineHeading = "This application will be saved until 19 May 2025"
  val title = s"$heading - Register for VAT - GOV.UK"
  val paragraph = "You will not be able to submit the application until you have completed the application."
  val ttDeadlineParagraph = "If you do not return to complete and submit this application by this date, you'll have to start the application again."
  val paragraph2 = "You can leave this page, or "
  val linkText = "return to your application"
  val insetText = "Saved applications will only be available for 7 days"

  val view: ApplicationProgressSaved = app.injector.instanceOf[ApplicationProgressSaved]

  "Application submission confirmation page with SubmitDeadline as false" should {
    appConfig.setValue(SubmitDeadline,"false")
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

  "Application submission confirmation page with SubmitDeadline as true" should {
    appConfig.setValue(SubmitDeadline,"true")
    implicit val doc: Document = Jsoup.parse(view().body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ttDeadlineHeading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ttDeadlineHeading)
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ttDeadlineParagraph)
      doc.para(2) mustBe Some(paragraph2 + linkText)
    }

    "have the correct Link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(linkText, "#"))
    }

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }
  }

}