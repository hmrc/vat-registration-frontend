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

import helpers.A11ySpec
import views.html.sicandcompliance.AboutToConfirmSic

class AboutToConfirmSicA11ySpec extends A11ySpec {
  val view: AboutToConfirmSic = app.injector.instanceOf[AboutToConfirmSic]

  "About to confirm sic page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view().toString must passAccessibilityChecks
      }
    }
  }
}
