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

import forms.ImportsOrExportsForm
import org.jsoup.Jsoup
import views.html.ImportsOrExports

class ImportsOrExportsViewSpec extends VatRegViewSpec {

  val title = "Will the business trade VAT-taxable goods with countries outside the UK?"
  val heading = "Will the business trade VAT-taxable goods with countries outside the UK?"
  val buttonText = "Save and continue"
  val viewInstance = app.injector.instanceOf[ImportsOrExports]

  "ImportsOrExports Page" should {
    lazy val form = ImportsOrExportsForm.form
    lazy val view = viewInstance(form)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text mustBe buttonText
    }
  }
}