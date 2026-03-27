/*
 * Copyright 2026 HM Revenue & Customs
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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.errors.BankDetailsLockoutView

class BankDetailsLockoutViewSpec extends VatRegViewSpec {

  val view: BankDetailsLockoutView = app.injector.instanceOf[BankDetailsLockoutView]

  val heading  = "Account details could not be verified"
  val para1    = "We have been unable to verify the account details you supplied."
  val para2    = "For your security, we have paused this part of the service."
  val linkText = "You can return to the VAT registration task list now"
  val para3    = ", and provide your account details later once your registration is confirmed."

  "BankDetailsLockoutView" should {

    implicit lazy val doc: Document = Jsoup.parse(view().body)

    "have the correct title" in new ViewSetup {
      doc.title must include(heading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct p1 text" in new ViewSetup {
      doc.para(1) mustBe Some(para1)
    }

    "have the correct p2 text" in new ViewSetup {
      doc.para(2) mustBe Some(para2)
    }

    "have the correct p3 text" in new ViewSetup {
      doc.para(3) mustBe Some(s"$linkText$para3")
    }

    "not show a back link" in new ViewSetup {
      doc.select(".govuk-back-link").size mustBe 0
    }
  }
}