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

import featureswitch.core.config.{FeatureSwitching, OptionToTax}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.attachments.Vat5LRequired

class Vat5LRequiredViewSpec extends VatRegViewSpec with FeatureSwitching {

  val vat5LRequiredPage: Vat5LRequired = app.injector.instanceOf[Vat5LRequired]
  lazy val view: Html = vat5LRequiredPage()

  object ExpectedContent {
    val heading = "You must send us a completed VAT5L form in order for us to process this application"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "We need a completed VAT5L form (opens in new tab) with details of the land and property supplies the business is making."
    val para1614 = "If you have decided to, or want to opt to tax land or buildings, we also need a completed VAT1614A (opens in new tab) or VAT1614H form (opens in new tab)"
    val paraSupportingDocs = "If you have any supporting documents, you need to send these too."
    val link = "VAT5L form (opens in new tab)"
    val continue = "Continue"
    val url = "https://www.gov.uk/government/publications/vat-vat-registration-land-and-property-vat-5l"
  }

  "The VAT5L Required page" must {
    implicit val doc = Jsoup.parse(view.body)
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

    "have vat1614 and supporting docs text when OptionToTax feature switch is on"  in new ViewSetup {
      enable(OptionToTax)
      override val doc: Document = Jsoup.parse(vat5LRequiredPage().body)
      doc.para(2) mustBe Some(ExpectedContent.para1614)
      doc.para(3) mustBe Some(ExpectedContent.paraSupportingDocs)
      disable(OptionToTax)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link, ExpectedContent.url))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
