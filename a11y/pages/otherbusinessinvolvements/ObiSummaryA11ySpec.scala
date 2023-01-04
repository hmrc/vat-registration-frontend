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

package pages.otherbusinessinvolvements

import forms.otherbusinessinvolvements.ObiSummaryForm
import helpers.A11ySpec
import viewmodels.ObiSummaryRow
import views.html.otherbusinessinvolvements.ObiSummary

class ObiSummaryA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ObiSummary]
  val testCompanyName = "testCompanyName"
  val changeLink = controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1)
  val removeLink = controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1)
  val summaryRow: ObiSummaryRow = ObiSummaryRow(
    businessName = testCompanyName,
    changeAction = changeLink,
    deleteAction = removeLink
  )
  val testList = List(summaryRow)

  "Obi summary page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(ObiSummaryForm(), testList, 1).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(ObiSummaryForm().bind(Map("value" -> "")), testList, 1).toString must passAccessibilityChecks
      }
    }
  }
}
