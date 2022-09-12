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

package views.otherbusinessinvolvements


import fixtures.VatRegistrationFixture
import forms.otherbusinessinvolvements.ObiSummaryForm
import org.jsoup.Jsoup
import viewmodels.ObiSummaryRow
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.ObiSummary

class ObiSummaryViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[ObiSummary]

  object ExpectedMessages {
    val heading = "You have added 1 business involvement"
    val maxOBILimitHeading = "You have added 10 business involvements"
    val changeLink = "Change"
    val removeLink = "Remove"
    val questionLabel = "Do you need to add another business?"
    val questionHint = "You must tell us about any other businesses the partners or directors are a sole trader, partner or director of in the United Kingdom or Isle of Man."
    val yes = "Yes"
    val no = "No"
    val submitButton = "Save and continue"
  }

  val changeLink = controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1)
  val removeLink = controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1)
  val testVatNumber = "testVatNumber"

  val summaryRow: ObiSummaryRow = ObiSummaryRow(
    businessName = testCompanyName,
    changeAction = changeLink,
    deleteAction = removeLink
  )

  "The OBI summary view" must {
    val testList = List(summaryRow)
    implicit val doc = Jsoup.parse(view(ObiSummaryForm(), testList, testList.size).body)

    "have the correct title" in new ViewSetup {
      doc.title must include (ExpectedMessages.heading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a single OBI" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 1
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

  "OBI summary view with max allowed OBI rows" must {
    val testList = List.fill(10)(summaryRow)
    implicit val doc = Jsoup.parse(view(ObiSummaryForm(), testList, testList.size).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(ExpectedMessages.maxOBILimitHeading)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.maxOBILimitHeading)
    }
    "have max limit of 10 OBIs" in new ViewSetup {
      doc.getElementsByTag("tr").size() mustBe 10
    }
    "does not have a Yes/No question" in new ViewSetup {
      doc.radioGroup(1) mustBe None
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.submitButton)
    }
  }
}
