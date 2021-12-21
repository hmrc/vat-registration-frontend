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

import fixtures.FlatRateFixtures
import org.jsoup.Jsoup
import views.html.frs_confirm_business_sector

class ConfirmBusinessTypeViewSpec extends VatRegViewSpec with FlatRateFixtures {

  val view = app.injector.instanceOf[frs_confirm_business_sector]

  implicit val doc = Jsoup.parse(view(testsector._1).body)

  "confirm business type" must {
    "have the correct title" in new ViewSetup {
      doc.title must include("Confirm the business type for the Flat Rate Scheme")
    }
    "have a h1 heading" in new ViewSetup {
      doc.heading mustBe Some("Confirm the business type for the Flat Rate Scheme")
    }
    "have a warning prompt" in new ViewSetup {
      doc.warningText(1) match {
        case Some(value) => value must include("It’s your legal responsibility to check the business type is the right one.")
        case None => fail()
      }
    }
    "have a first paragraph" in new ViewSetup {
      doc.para(1) mustBe Some("We’ve based this on what we already know about the business:")
    }
    "have a h2 heading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some("Business type")
    }
    "have a second paragraph" in new ViewSetup {
      doc.para(2) mustBe Some(testsector._1)
    }
    "have a link to change the business type" in new ViewSetup {
      doc.link(1) mustBe Some(Link("Change the business type", "/register-for-vat/choose-business-type"))
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some("Confirm and continue")
    }
  }

}
