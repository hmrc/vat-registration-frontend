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

package views.bankdetails

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.AccountDetailsNotVerifiedView

class AccountDetailsNotVerifiedViewSpec extends VatRegViewSpec {

  val view: AccountDetailsNotVerifiedView = app.injector.instanceOf[AccountDetailsNotVerifiedView]

  val heading      = "We could not verify the bank details you provided"
  val textPre      = "You have"
  val textPost     = "to provide your account details."
  val oneRemaining = "one more attempt"
  val twoRemaining = "two more attempts"
  val para3Bold    = "After 3 consecutive unsuccessful attempts,"
  val para3        = "you will need to complete your VAT registration before sending us your details."

  "AccountDetailsNotVerifiedView with 1 attempt used" should {

    implicit lazy val doc: Document = Jsoup.parse(view(1).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(heading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "show two more attempts in bold" in new ViewSetup {
      doc.select("strong").first().text mustBe twoRemaining
    }

    "have the correct attempts paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(s"$textPre $twoRemaining $textPost")
    }

    "have the correct link in para2" in new ViewSetup {
      doc.select("a").text must include("Enter your bank account or building society details again,")
    }

    "have the correct para3 text" in new ViewSetup {
      doc.para(3) mustBe Some(s"$para3Bold $para3")
    }
  }

  "AccountDetailsNotVerifiedView with 2 attempts used" should {

    implicit lazy val doc: Document = Jsoup.parse(view(2).body)

    "have the correct attempts paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(s"$textPre $oneRemaining $textPost")
    }
  }
}
