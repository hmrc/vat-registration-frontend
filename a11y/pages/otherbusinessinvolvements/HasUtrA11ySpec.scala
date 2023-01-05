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

import forms.otherbusinessinvolvements.HasUtrForm
import helpers.A11ySpec
import views.html.otherbusinessinvolvements.HasUtr

class HasUtrA11ySpec extends A11ySpec {
  val view: HasUtr = app.injector.instanceOf[HasUtr]

  "Has Utr page" when {
    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(HasUtrForm().fill(true), 1).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(HasUtrForm(), 1).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(HasUtrForm().bind(Map("value" -> "")), 1).toString must passAccessibilityChecks
      }
    }
  }
}
