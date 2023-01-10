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

import forms.SupplyWorkersForm
import helpers.A11ySpec
import views.html.sicandcompliance.SupplyWorkers

class SupplyWorkersA11ySpec extends A11ySpec {
  val view: SupplyWorkers = app.injector.instanceOf[SupplyWorkers]

  "Supply workers page" when {
    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(SupplyWorkersForm.form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(SupplyWorkersForm.form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(SupplyWorkersForm.form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}
