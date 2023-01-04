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

import forms.otherbusinessinvolvements.CaptureVrnForm
import helpers.A11ySpec
import views.html.otherbusinessinvolvements.CaptureVrn

class CaptureVrnA11ySpec extends A11ySpec {
  val view: CaptureVrn = app.injector.instanceOf[CaptureVrn]

  "Capture Vrn page" when {
    "the page is rendered without errors when a VRN is given" must {
      "pass all accessibility tests" in {
        view(CaptureVrnForm().fill("123456782"), 1).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when no VRN is given" must {
      "pass all accessibility tests" in {
        view(CaptureVrnForm(), 1).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(CaptureVrnForm().bind(Map("captureVrn" -> "")), 1).toString must passAccessibilityChecks
      }
    }
  }
}
