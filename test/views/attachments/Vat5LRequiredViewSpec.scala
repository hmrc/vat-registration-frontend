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

package views.attachments

import org.jsoup.Jsoup
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.attachments.Vat5LRequired

class Vat5LRequiredViewSpec extends VatRegViewSpec {

  val vat2RequiredPage: Vat5LRequired = app.injector.instanceOf[Vat5LRequired]


  lazy val view: Html = vat2RequiredPage()
  implicit val doc = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "You must send us a completed VAT5L form in order for us to process this application"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "We need a completed VAT5L form (opens in new tab) to capture all of the land and property supplies that the business is making. Once you have finished this form you must either upload, email or post a copy to HRMC."
    val link = "VAT5L form (opens in new tab)"
    val continue = "Save and continue"
    val url = "https://www.gov.uk/government/publications/vat-vat-registration-land-and-property-vat-5l"
  }

  "The VAT5L Required page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link, ExpectedContent.url))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
