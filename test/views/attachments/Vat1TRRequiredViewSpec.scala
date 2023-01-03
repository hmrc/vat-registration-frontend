/*
 * Copyright 2023 HM Revenue & Customs
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
import views.html.attachments.Vat1TRRequired

class Vat1TRRequiredViewSpec extends VatRegViewSpec {

  val vat1TRRequiredPage: Vat1TRRequired = app.injector.instanceOf[Vat1TRRequired]


  lazy val view: Html = vat1TRRequiredPage()
  implicit val doc = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "You must send us a completed VAT1TR form in order for us to process this application"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "We need a completed VAT1TR form (opens in new tab) with details of your chosen UK tax representative."
    val link = "VAT1TR form (opens in new tab)"
    val continue = "Continue"
    val url = "https://www.gov.uk/government/publications/vat-appointment-of-tax-representative-vat1tr"
  }

  "The VAT1TR Required page" must {
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
