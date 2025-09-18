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

import common.enums.VatRegStatus
import featuretoggle.FeatureSwitch.SubmitDeadline
import fixtures.VatRegistrationFixture
import models.api.VatSchemeHeader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.ManageRegistrations

class ManageRegistrationsViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view: ManageRegistrations = app.injector.instanceOf[ManageRegistrations]

  val testApplicationReference = "testApplicationReference"

  val testVatSchemeHeader: VatSchemeHeader = VatSchemeHeader(
    registrationId = testRegId,
    status = VatRegStatus.draft,
    applicationReference = Some(testApplicationReference),
    createdDate = testDate,
    requiresAttachments = false
  )


  object ExpectedMessages {
    val heading = "Manage your VAT registration applications"
    val panelIndent = "In progress registrations will only be available for completion 7 days from the day you last saved the application."
    val ttDeadlineIndent = "In progress registrations must be completed and submitted by 19 May 2025. Otherwise, you'll have to start all registrations again."
    val referenceHeading = "Reference"
    val dateHeading = "Date created"
    val statusHeading = "Status"
    val startNewLink = "Create a new application"
  }

  val tableLinkSelector = "tr td a"

  "the Manage Registrations page" must {

    appConfig.setValue(SubmitDeadline,"false")
    implicit val draftDoc: Document = Jsoup.parse(view(List(testVatSchemeHeader)).body)

    "have the right title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the right H1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a panel indent with the correct text" in new ViewSetup {
      doc.select(Selectors.indent).text() mustBe ExpectedMessages.panelIndent
    }
    "have a link to start a new journey" in new ViewSetup {
      doc.select("main a").get(1).text mustBe ExpectedMessages.startNewLink
    }
    "have a table that" when {
      "rendered have the correct table headings" in new ViewSetup {
        doc.select("th").text() mustBe s"${ExpectedMessages.referenceHeading} ${ExpectedMessages.dateHeading} ${ExpectedMessages.statusHeading}"
      }
      "have a link to start the selected journey" in new ViewSetup {
        val link: Elements = doc.select(tableLinkSelector)
        link.text mustBe testApplicationReference
        link.attr("href") mustBe controllers.routes.JourneyController.continueJourney(Some(testRegId)).url
      }
    }
  }

  "the Manage Registrations page with SubmitDeadline enabled" must {

    appConfig.setValue(SubmitDeadline,"true")
    implicit val draftDoc1: Document = Jsoup.parse(view(List(testVatSchemeHeader)).body)

    "have the right title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the right H1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a panel indent with the correct text" in new ViewSetup {
      doc.select(Selectors.indent).text() mustBe ExpectedMessages.ttDeadlineIndent
    }
    "have a link to start a new journey" in new ViewSetup {
      doc.select("main a").get(1).text mustBe ExpectedMessages.startNewLink
    }
    "have a table that" when {
      "rendered have the correct table headings" in new ViewSetup {
        doc.select("th").text() mustBe s"${ExpectedMessages.referenceHeading} ${ExpectedMessages.dateHeading} ${ExpectedMessages.statusHeading}"
      }
      "have a link to start the selected journey" in new ViewSetup {
        val link: Elements = doc.select(tableLinkSelector)
        link.text mustBe testApplicationReference
        link.attr("href") mustBe controllers.routes.JourneyController.continueJourney(Some(testRegId)).url
      }
    }
  }

}
