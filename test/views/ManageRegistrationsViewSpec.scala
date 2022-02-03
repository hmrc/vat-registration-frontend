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

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import models.api.VatSchemeHeader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ManageRegistrations

class ManageRegistrationsViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[ManageRegistrations]

  val testApplicationReference = "testApplicationReference"

  val testVatSchemeHeader = VatSchemeHeader(
    registrationId = testRegId,
    status = VatRegStatus.draft,
    applicationReference = Some(testApplicationReference),
    createdDate = Some(testDate),
    requiresAttachments = false
  )

  implicit val draftDoc = Jsoup.parse(view(List(testVatSchemeHeader)).body)

  object ExpectedMessages {
    val heading = "Manage your VAT registration applications"
    val panelIndent = "In progress registrations will only be available for completion 7 days from the day you last saved the application."
    val referenceHeading = "Reference"
    val dateHeading = "Date created"
    val statusHeading = "Status"
    val startNewLink = "Create a new application"
  }

  val tableLinkSelector = "tr td a"

  "the Manage Registrations page" must {
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
        val link = doc.select(tableLinkSelector)
        link.text mustBe testApplicationReference
        link.attr("href") mustBe controllers.routes.JourneyController.continueJourney(Some(testRegId)).url
      }
    }
  }

}
