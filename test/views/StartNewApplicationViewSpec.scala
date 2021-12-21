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

package views

import forms.StartNewApplicationForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.pages.start_new_application

class StartNewApplicationViewSpec extends VatRegViewSpec {

  val heading = "Are you starting a new application?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val radio1 = "Yes"
  val radio2 = "No - I already started one"

  val view: start_new_application = app.injector.instanceOf[start_new_application]

  "Start New Application page" should {
    implicit val doc: Document = Jsoup.parse(view(StartNewApplicationForm.form).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct radio buttons" in new ViewSetup {
      doc.radio("true") mustBe Some(radio1)
      doc.radio("false") mustBe Some(radio2)
    }
  }
}
