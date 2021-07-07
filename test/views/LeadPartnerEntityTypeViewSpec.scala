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

import featureswitch.core.config.{FeatureSwitching, SaveAndContinueLater}
import forms.LeadPartnerForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.lead_partner_entity_type

class LeadPartnerEntityTypeViewSpec extends VatRegViewSpec with FeatureSwitching {

  object ExpectedContent {
    val heading: String = "What type of entity is the lead partner?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button1: String = "Sole trader"
    val button2: String = "Non-established taxable person (NETP)"
    val button3: String = "UK company"
    val button4: String = "Scottish partnership"
    val button5: String = "Scottish limited partnership"
    val button6: String = "Limited liability partnership"
    val button7: String = "Charitable Incorporated Organisation (CIO)"
    val button8: String = "Registered society"
    val error: String = "Select the entity type of the lead partner"
    val continue: String = "Save and continue"
    val continueLater: String = "Save and come back later"
  }

  "Lead Partner Entity Type Page" should {

    enable(SaveAndContinueLater)

    val view: lead_partner_entity_type = app.injector.instanceOf[lead_partner_entity_type]
    implicit val doc: Document = Jsoup.parse(view(LeadPartnerForm.form).body)

    disable(SaveAndContinueLater)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("Z1") mustBe Some(ExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("55") mustBe Some(ExpectedContent.button2)
    }

    "have the correct button3" in new ViewSetup() {
      doc.radio("50") mustBe Some(ExpectedContent.button3)
    }

    "have the correct button4" in new ViewSetup() {
      doc.radio("58") mustBe Some(ExpectedContent.button4)
    }

    "have the correct button5" in new ViewSetup() {
      doc.radio("59") mustBe Some(ExpectedContent.button5)
    }

    "have the correct button6" in new ViewSetup() {
      doc.radio("52") mustBe Some(ExpectedContent.button6)
    }

    "have the correct button7" in new ViewSetup() {
      doc.radio("53") mustBe Some(ExpectedContent.button7)
    }

    "have the correct button8" in new ViewSetup() {
      doc.radio("54") mustBe Some(ExpectedContent.button8)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }

  }

}
