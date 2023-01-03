/*
 * Copyright 2023 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.OtherBusinessCheckAnswers

class OtherBusinessCheckAnswersSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view: OtherBusinessCheckAnswers = app.injector.instanceOf[OtherBusinessCheckAnswers]

  val testIndex = 1
  val testVatNumber = "testVatNumber"
  val testUtr = "testUtr"
  val activelyTrading = false

  object ExpectedMessages {
    def title(changeMode: Boolean): String = if (changeMode) {
      s"Change $testCompanyName details"
    } else {
      "Check your answers"
    }

    val businessNameRow = "Name of other business"
    val hasVrnRow = "Other business has VAT number"
    val vrnRow = "VAT number for other business"
    val hasUtrRow = "Other business has Unique Taxpayer Reference"
    val utrRow = "Unique Taxpayer Reference for other business"
    val activelyTradingRow = "Other business still trading"
    val change = "Change"
    val submit = "Save and continue"
  }

  def document(changeMode: Boolean, vatNumber: Option[String] = Some(testVatNumber), utr: Option[String] = None): Document = {
    val optHasUtr: Option[Boolean] = (vatNumber.isDefined, utr) match {
      case (true, _) => None
      case (false, Some(_)) => Some(true)
      case (false, None) => Some(false)
    }
    Jsoup.parse(view(testIndex, testCompanyName, vatNumber.isDefined, vatNumber, optHasUtr, utr, activelyTrading, changeMode).body)
  }

  "the Other Business Involvements Check Your Answers page" when {
    "not in 'change' mode" must {
      "have the correct title" in new ViewSetup()(document(changeMode = false)) {
        doc.title() must include(ExpectedMessages.title(changeMode = false))
      }
      "have the correct page heading" in new ViewSetup()(document(changeMode = false)) {
        doc.heading mustBe Some(ExpectedMessages.title(changeMode = false))
      }
    }
    "in 'change' mode" must {
      "have the correct title" in new ViewSetup()(document(changeMode = true)) {
        doc.title() must include(ExpectedMessages.title(changeMode = true))
      }
      "have the correct page heading" in new ViewSetup()(document(changeMode = true)) {
        doc.heading mustBe Some(ExpectedMessages.title(changeMode = true))
      }
    }
    "in any case" must {
      "show the business name answer" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(0) mustBe Some(SummaryRow(
          ExpectedMessages.businessNameRow,
          testCompanyName,
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(testIndex).url))
        ))
      }
      "show the right VRN and UTR answers when user has VRN" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(1) mustBe Some(SummaryRow(
          ExpectedMessages.hasVrnRow,
          "Yes",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(testIndex).url))
        ))
        doc.summaryRow(2) mustBe Some(SummaryRow(
          ExpectedMessages.vrnRow,
          testVatNumber,
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.CaptureVrnController.show(testIndex).url))
        ))
      }
      "show the right VRN and UTR answers when user has UTR without VRN" in new ViewSetup()(document(changeMode = false, vatNumber = None, utr = Some(testUtr))) {
        doc.summaryRow(1) mustBe Some(SummaryRow(
          ExpectedMessages.hasVrnRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(testIndex).url))
        ))
        doc.summaryRow(2) mustBe Some(SummaryRow(
          ExpectedMessages.hasUtrRow,
          "Yes",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HasUtrController.show(testIndex).url))
        ))
        doc.summaryRow(3) mustBe Some(SummaryRow(
          ExpectedMessages.utrRow,
          testUtr,
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HasUtrController.show(testIndex).url))
        ))
      }
      "show the right VRN and UTR answers when user has no UTR or VRN" in new ViewSetup()(document(changeMode = false, vatNumber = None, utr = None)) {
        doc.summaryRow(1) mustBe Some(SummaryRow(
          ExpectedMessages.hasVrnRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(testIndex).url))
        ))
        doc.summaryRow(2) mustBe Some(SummaryRow(
          ExpectedMessages.hasUtrRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.HasUtrController.show(testIndex).url))
        ))
      }
      "show the actively trading answer" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(3) mustBe Some(SummaryRow(
          ExpectedMessages.activelyTradingRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(testIndex).url))
        ))
      }
      "have a Save and Continue button" in new ViewSetup()(document(changeMode = false)) {
        doc.submitButton mustBe Some(ExpectedMessages.submit)
      }
    }
  }

}
