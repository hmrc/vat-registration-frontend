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
import models.Entity
import models.api.UkCompany
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.partners.PartnerSummary

class PartnerSummaryViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view: PartnerSummary = app.injector.instanceOf[PartnerSummary]

  object ExpectedMessages {
    val leadPartnerHeading = "Lead partner"
    val otherPartnersHeading = "Additional partners"
    val heading = "You have added 1 member of the partnership"
    val headingPlural = "You have added 2 members of the partnership"
    val changeLink = "Change"
    val removeLink = "Remove"
    val subheading = "You need to add another partner"
    val questionLabel = "Do you need to add another partner?"
    val questionHint = "You need a minimum of 2 partners. You can add a maximum of 10 members using this service."
    val yes = "Yes"
    val no = "No"
    val submitButton = "Save and continue"
  }

  def changeLink(idx: Int): String = controllers.partners.routes.PartnerEntityTypeController.showPartnerType(idx).url

  def removeLink(idx: Int): String = controllers.partners.routes.RemovePartnerEntityController.show(idx).url

  val testEmail = "test@email.com"
  val testPhoneNumber = "1234567890"

  "Partner summary view with two partners" must {
    val testList = List(
      Entity(Some(testLimitedCompany), UkCompany, Some(true), None, None, None, None),
      Entity(Some(testLimitedCompany), UkCompany, Some(false), None, Some(validCurrentAddress), Some(testEmail), Some(testPhoneNumber))
    )
    implicit val doc: Document = Jsoup.parse(view(PartnerSummaryForm(), testList, testList.size).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.headingPlural)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.headingPlural)
    }
    "have the correct h2 headings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedMessages.leadPartnerHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedMessages.otherPartnersHeading)
    }
    "have two partners" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 2
    }
    "have correct table headings" in new ViewSetup {
      doc.select("th").toList.map(_.text()) mustBe List(testCompanyName, testCompanyName)
    }
    "have a change link with the correct URL" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.changeLink, changeLink(2)))
    }
    "have a remove link with the correct URL" in new ViewSetup {
      doc.link(2) mustBe Some(Link(ExpectedMessages.removeLink, removeLink(2)))
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

  "Partner summary view with only one partner" must {
    val testList = List(
      Entity(Some(testLimitedCompany), UkCompany, Some(true), None, None, None, None)
    )
    implicit val doc: Document = Jsoup.parse(view(PartnerSummaryForm(), testList, testList.size).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct h2 headings" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedMessages.leadPartnerHeading)
      doc.headingLevel2(2) mustBe Some(ExpectedMessages.subheading)
    }
    "have the correct para" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.questionHint)
    }
    "have a single partner" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 1
    }
    "have correct table heading" in new ViewSetup {
      doc.select("th").toList.map(_.text()) mustBe List(testCompanyName)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.submitButton)
    }
  }
}
