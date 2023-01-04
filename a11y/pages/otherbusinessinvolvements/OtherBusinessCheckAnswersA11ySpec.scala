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

import helpers.A11ySpec
import views.html.otherbusinessinvolvements.OtherBusinessCheckAnswers

class OtherBusinessCheckAnswersA11ySpec extends A11ySpec {

  val view: OtherBusinessCheckAnswers = app.injector.instanceOf[OtherBusinessCheckAnswers]
  val testIndex = 1
  val testCompanyName = "testCompanyName"
  val hasVrn = true
  val testVatNumber = "testVatNumber"
  val hasUtr = Some(true)
  val testUtr = "testUtr"
  val activelyTrading = false
  val changeMode = false

  "Other business check answers page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(testIndex, testCompanyName, hasVrn, Some(testVatNumber), hasUtr, Some(testUtr), activelyTrading, changeMode).toString must passAccessibilityChecks
      }
    }
  }
}
