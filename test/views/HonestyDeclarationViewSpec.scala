/*
 * Copyright 2020 HM Revenue & Customs
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

import org.jsoup.Jsoup
import views.html.honesty_declaration

class HonestyDeclarationViewSpec extends VatRegViewSpec {

  val title = "Declaration - Register for VAT - GOV.UK"
  val header = "Declaration"
  val text = "By submitting this application to register for VAT, you are making a legal declaration that the information is correct and complete to the best of your knowledge and belief. A false declaration can result in prosecution."
  val buttonText = "Accept and continue"

  "Honesty Declaration Page" must {
    val view = new honesty_declaration(
      layout,
      h1,
      p,
      button,
      formWithCSRF
    ).apply(testCall)

    val doc = Jsoup.parse(view.body)

    "have the right title" in {
      doc.title() mustBe title
    }

    "have the right header" in {
      doc.select(Selectors.h1).text() mustBe header
    }

    "have the right text" in {
      doc.select(Selectors.p(1)).text() mustBe text
    }

    "have the right button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }
  }
}
