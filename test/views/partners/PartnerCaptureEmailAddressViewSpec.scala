/*
 * Copyright 2024 HM Revenue & Customs
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

package views.partners

import forms.PartnerEmailAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.VatRegViewSpec
import views.html.partners.PartnerCaptureEmailAddress

class PartnerCaptureEmailAddressViewSpec extends VatRegViewSpec {

  val form: Form[String] = PartnerEmailAddressForm.form
  val view: PartnerCaptureEmailAddress = app.injector.instanceOf[PartnerCaptureEmailAddress]
  implicit val doc: Document = Jsoup.parse(view(form, 2, Some("testFirstName")).body)

  val title = "What is testFirstName’s email address? - Register for VAT - GOV.UK"
  val heading = "What is testFirstName’s email address?"
  val para = "Full details of how we use your information are in the HMRC Privacy Notice (opens in new tab)"
  val link = "HMRC Privacy Notice (opens in new tab)"
  val buttonText = "Save and continue"

  "Partner Capture Email Address Page" should {

    "have the correct title" in new ViewSetup {
      doc.title mustBe title
    }

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, appConfig.privacyNoticeUrl))
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
