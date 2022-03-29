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
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.OtherBusinessCheckAnswers

class OtherBusinessCheckAnswersSpec extends VatRegViewSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[OtherBusinessCheckAnswers]

  val testIndex = 1
  val testVatNumber = "testVatNumber"
  val activelyTrading = false

  object ExpectedMessages {
    def title(changeMode: Boolean) = if (changeMode) {
      s"Change $testCompanyName details"
    } else {
      "Check your answers"
    }

    val businessNameRow = "Business name"
    val hasVrnRow = "Other business has VAT number?"
    val vrnRow = "VAT number"
    val activelyTradingRow = "Still actively trading"
    val change = "Change"
    val submit = "Save and continue"
  }

  def document(changeMode: Boolean, vatNumber: Option[String] = Some(testVatNumber)) =
    Jsoup.parse(view(testIndex, testCompanyName, vatNumber.isDefined, vatNumber, activelyTrading, changeMode).body)

  "the Other Business Involvements Check Your Answers page" when {
    "not in 'change' mode" must {
      "have the correct title" in new ViewSetup()(document(changeMode = false)) {
        doc.title() must include (ExpectedMessages.title(changeMode = false))
      }
      "have the correct page heading" in new ViewSetup()(document(changeMode = false)) {
        doc.heading mustBe Some(ExpectedMessages.title(changeMode = false))
      }
    }
    "in 'change' mode" must {
      "have the correct title" in new ViewSetup()(document(changeMode = true)) {
        doc.title() must include (ExpectedMessages.title(changeMode = true))
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
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.OtherBusinessNameController.show(testIndex).url))
        ))
      }
      "show the Has VRN answer" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(1) mustBe Some(SummaryRow(
          ExpectedMessages.hasVrnRow,
          "Yes",
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.HaveVatNumberController.show(testIndex).url))
        ))
      }
      "show the VRN answer if a VRN was provided" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(2) mustBe Some(SummaryRow(
          ExpectedMessages.vrnRow,
          testVatNumber,
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.CaptureVrnController.show(testIndex).url))
        ))
      }
      "not show the VRN answer if a VRN wasn't provided" in new ViewSetup()(document(changeMode = false, vatNumber = None)) {
        doc.summaryRow(1) mustBe Some(SummaryRow(
          ExpectedMessages.hasVrnRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.HaveVatNumberController.show(testIndex).url))
        ))
        doc.summaryRow(2) mustBe Some(SummaryRow(
          ExpectedMessages.activelyTradingRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(testIndex).url))
        ))
        doc.summaryRow(3) mustBe None
      }
      "show the actively trading answer" in new ViewSetup()(document(changeMode = false)) {
        doc.summaryRow(3) mustBe Some(SummaryRow(
          ExpectedMessages.activelyTradingRow,
          "No",
          Seq(Link(ExpectedMessages.change, controllers.registration.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(testIndex).url))
        ))
      }
      "have a Save and Continue button" in new ViewSetup()(document(changeMode = false)) {
        doc.submitButton mustBe Some(ExpectedMessages.submit)
      }
    }
  }

}
