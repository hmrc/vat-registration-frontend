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

package views.partners

import fixtures.VatRegistrationFixture
import forms.partners.PartnerSummaryForm
import org.jsoup.Jsoup
import viewmodels.PartnerSummaryRow
import views.VatRegViewSpec
import views.html.partners.PartnerSummary

class PartnerSummaryViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[PartnerSummary]

  object ExpectedMessages {
    val heading = "You have added 1 member of the partnership"
    val maxPartnersLimitHeading = "You have added 10 members of the partnership"
    val changeLink = "Change"
    val removeLink = "Remove"
    val questionLabel = "Do you need to add another partner?"
    val questionHint = "You need a minimum of 2 partners. You can add a maximum of 10 members using this service."
    val yes = "Yes"
    val no = "No"
    val submitButton = "Save and continue"
  }

  val changeLink = controllers.partners.routes.PartnerEntityTypeController.showPartnerType(1)
  val removeLink = controllers.partners.routes.RemovePartnerEntityController.show(1)
  val testVatNumber = "testVatNumber"

  val summaryRow: PartnerSummaryRow = PartnerSummaryRow(
    name = Some(testCompanyName),
    changeAction = Some(changeLink),
    deleteAction = Some(removeLink)
  )

  "Partner summary view" must {
    val testList = List(summaryRow)
    implicit val doc = Jsoup.parse(view(PartnerSummaryForm(), testList, testList.size).body)

    "have the correct title" in new ViewSetup {
      doc.title must include (ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a single OBI" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 1
    }
    "have correct table heading" in new ViewSetup {
      doc.select("th").text() mustBe testCompanyName
    }
    "have a change link with the correct URL" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.changeLink, changeLink.url))
    }
    "have a remove link with the correct URL" in new ViewSetup {
      doc.link(2) mustBe Some(Link(ExpectedMessages.removeLink, removeLink.url))
    }
    "have a Yes/No question with the correct content" in new ViewSetup {
      doc.radioGroup(1) mustBe Some(RadioGroup(
        legend = ExpectedMessages.questionLabel,
        hint = Some(ExpectedMessages.questionHint),
        options = List(ExpectedMessages.yes, ExpectedMessages.no)
      ))
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.submitButton)
    }
  }
}
