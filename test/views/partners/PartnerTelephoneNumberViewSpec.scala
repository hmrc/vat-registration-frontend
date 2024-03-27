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

import forms.PartnerTelephoneForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.partners.PartnerTelephoneNumber

class PartnerTelephoneNumberViewSpec extends VatRegViewSpec {

  val form = PartnerTelephoneForm.form
  val view = app.injector.instanceOf[PartnerTelephoneNumber]
  implicit val doc = Jsoup.parse(view(form, 2, Some("test name")).body)

  object ExpectedContent {
    val heading = "What is test name’s telephone number?"
    val continue = "Save and continue"
  }

  "The Partner telephone number view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
