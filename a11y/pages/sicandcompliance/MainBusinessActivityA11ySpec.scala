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

import forms.MainBusinessActivityForm
import helpers.A11ySpec
import models.api.SicCode
import views.html.sicandcompliance.MainBusinessActivity

class MainBusinessActivityA11ySpec extends A11ySpec {
  val view: MainBusinessActivity = app.injector.instanceOf[MainBusinessActivity]
  val sicCodeList = Seq(SicCode("id1", "code1", "code display 1"), SicCode("id2", "code2", "code display 2"), SicCode("id3", "code3", "code display 3"))

  "Main business activity page" when {
    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(MainBusinessActivityForm.form, sicCodeList).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(MainBusinessActivityForm.form, sicCodeList).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors when missing value" must {
      "pass all accessibility tests" in {
        view(MainBusinessActivityForm.form.bind(Map("mainBusinessActivity" -> "")), sicCodeList).toString must passAccessibilityChecks
      }
    }
  }
}
