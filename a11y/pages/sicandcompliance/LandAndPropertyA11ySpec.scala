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

package pages.sicandcompliance

import forms.LandAndPropertyForm
import helpers.A11ySpec
import views.html.sicandcompliance.LandAndProperty

class LandAndPropertyA11ySpec extends A11ySpec {
  val view: LandAndProperty = app.injector.instanceOf[LandAndProperty]

  "Land and property page" when {
    "the page is rendered without errors when a UTR is given" must {
      "pass all accessibility tests" in {
        view(LandAndPropertyForm.form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when no UTR is given" must {
      "pass all accessibility tests" in {
        view(LandAndPropertyForm.form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(LandAndPropertyForm.form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}
