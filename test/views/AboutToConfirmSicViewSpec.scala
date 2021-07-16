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
import org.jsoup.nodes.Document
import views.html.about_to_confirm_sic

class AboutToConfirmSicViewSpec extends VatRegViewSpec {

  val view: about_to_confirm_sic = app.injector.instanceOf[about_to_confirm_sic]

  val heading = "You are about to confirm the business’s Standard Industry Classification (SIC) codes for VAT"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1 = "A SIC code describes a business activity."
  val para2 = "Every business has one or more SIC codes from when it registered with Companies House."
  val para3 = "You must:"
  val para4 = "We will guide you through this process."
  val bullet1 = "check the SIC codes for this business are still correct."
  val bullet2 = "add any more SIC codes that are relevant to the business’s activity."
  val continue = "Save and continue"

  implicit val doc: Document = Jsoup.parse(view().body)

  "The Join FRS page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para1)
      doc.para(2) mustBe Some(para2)
      doc.para(3) mustBe Some(para3)
      doc.para(4) mustBe Some(para4)
    }

    "have correct bullets" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        bullet1,
        bullet2
      )
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}
