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

package views.partners

import org.jsoup.Jsoup
import views.VatRegViewSpec
import org.jsoup.nodes.Document
import forms.partners.RemovePartnerEntityForm
import views.html.partners.RemovePartnerEntity

class RemovePartnerEntityViewSpec extends VatRegViewSpec {
  val view: RemovePartnerEntity = app.injector.instanceOf[RemovePartnerEntity]

  object ExpectedContent {
    val testPartnerName = "testPartnerName"
    val heading = s"Are you sure you want to remove $testPartnerName?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(RemovePartnerEntityForm(Some(ExpectedContent.testPartnerName)).form, Some(ExpectedContent.testPartnerName), 1).body)

  "Remove partner entity page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
