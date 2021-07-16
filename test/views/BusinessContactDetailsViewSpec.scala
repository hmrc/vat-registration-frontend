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

import forms.CompanyContactDetailsForm
import models.CompanyContactDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.business_contact_details

class BusinessContactDetailsViewSpec extends VatRegViewSpec {

  lazy val view: business_contact_details = app.injector.instanceOf[business_contact_details]
  lazy val form: Form[CompanyContactDetails] = CompanyContactDetailsForm.form
  implicit val doc: Document = Jsoup.parse(view(form).body)

  val heading = "Business contact details"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1 = "We may need to get in touch for more information about the business’s VAT affairs. Enter contact details for someone from the business."
  val para2 = "You need to provide a contact email address."
  val indentText = "You need to provide at least one of the following contact numbers."

  val label1 = "Email address"
  val label2 = "Phone number"
  val label3 = "Mobile number"
  val label4 = "Website address (optional)"

  val continue = "Save and continue"

  "Former Name Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have correct text for paragraph1" in new ViewSetup {
      doc.select(Selectors.p(1)).get(0).text mustBe para1
    }

    "have correct text for paragraph2" in new ViewSetup {
      doc.select(Selectors.p(1)).get(1).text mustBe para2
    }

    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe indentText
    }

    "have a textbox label for email" in new ViewSetup {
      doc.textBox("email") mustBe Some(label1)
    }

    "have a textbox label for phone" in new ViewSetup {
      doc.textBox("daytimePhone") mustBe Some(label2)
    }

    "have a textbox label for mobile" in new ViewSetup {
      doc.textBox("mobile") mustBe Some(label3)
    }

    "have a textbox label for website" in new ViewSetup {
      doc.textBox("website") mustBe Some(label4)
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }
}