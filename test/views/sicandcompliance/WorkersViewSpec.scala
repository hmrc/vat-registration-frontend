/*
 * Copyright 2024 HM Revenue & Customs
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

package views.sicandcompliance

import forms.WorkersForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.sicandcompliance.Workers

class WorkersViewSpec extends VatRegViewSpec {

  lazy val view: Workers = app.injector.instanceOf[Workers]
  implicit val doc: Document = Jsoup.parse(view(WorkersForm.form).body)

  object ExpectedContent {
    val heading = "How many workers does the business supply?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button = "Save and continue"
  }

  "Workers Page" should {
    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }
    "have a label that's the same as the heading" in new ViewSetup {
      doc.textBox("numberOfWorkers") mustBe Some(ExpectedContent.heading)
    }
    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.button)
    }
  }

}
