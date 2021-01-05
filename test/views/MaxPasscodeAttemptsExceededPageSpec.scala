/*
 * Copyright 2021 HM Revenue & Customs
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
import views.html.pages.error.maxPasscodeAttemptsExceeded

class MaxPasscodeAttemptsExceededPageSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[maxPasscodeAttemptsExceeded]
  implicit val doc = Jsoup.parse(view().body)

  object ExpectedContent {
    val title = "You need to start again - Register for VAT - GOV.UK"
    val heading = "You need to start again"
    val p1 = "This is because you have entered the wrong code too many times."
    val p2 = "To start again you need to sign out."
    val link = "sign out"
  }

  "MaxPasscodeAttemptsExceededPage" should {
    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }
    "have the right header" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }
    "have the correct text in the primary paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
    }
    "have the correct text in the secondary paragraph" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedContent.p2)
    }
    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link, "/register-for-vat/email-address"))
    }
  }

}
