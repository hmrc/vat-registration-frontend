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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.pages.error.passcode_not_found

class PasscodeNotFoundViewSpec extends VatRegViewSpec {

  val view: passcode_not_found = app.injector.instanceOf[passcode_not_found]
  val testUrl = "test"

  val heading = "You need to start again"
  val title = s"$heading - Register for VAT - GOV.UK"
  val p1 = "The code we send you cannot be found or has expired."
  val linkText = "get a new code"
  val p2 = s"You will need to get a new code to continue with the registration."

  implicit val doc: Document = Jsoup.parse(view(testUrl).body)

  "The Join FRS page" must {
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have the correct primary text" in new ViewSetup {
      doc.para(1) mustBe Some(p1)
    }

    "have the correct secondary text" in new ViewSetup {
      doc.para(2) mustBe Some(p2)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(linkText, testUrl))
    }

  }
}
