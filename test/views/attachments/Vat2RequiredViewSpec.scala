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
import views.html.attachments.Vat2Required

class Vat2RequiredViewSpec extends VatRegViewSpec {

  val vat2RequiredPage: Vat2Required = app.injector.instanceOf[Vat2Required]


  lazy val view: Html = vat2RequiredPage()
  implicit val doc = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "You must complete a VAT 2 form"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "You must now download the VAT 2 form (opens in new tab) to provide additional details of the partners included in this partnership’s VAT registration. Once you have finished this form you must either upload, email or post a copy to HMRC."
    val link = "download the VAT 2 form (opens in new tab)"
    val continue = "Save and continue"
    val url = "https://www.gov.uk/government/publications/vat-partnership-details-vat2"
  }

  "The VAT 2 Required page" must {
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
