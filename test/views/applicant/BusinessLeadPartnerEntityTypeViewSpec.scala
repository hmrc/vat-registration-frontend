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

package views.applicant

import featureswitch.core.config.{FeatureSwitching, SaveAndContinueLater}
import forms.LeadPartnerForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.applicant.partnership_business_entity_type

class BusinessLeadPartnerEntityTypeViewSpec extends VatRegViewSpec with FeatureSwitching {

  implicit val errorKey: String = "pages.businessLeadPartnerEntityType.missing"
  
  object ExpectedContent {
    val heading: String = "What type of business are you within the partnership?"
    val heading3pt: String = "What type of business is the lead partner within the partnership?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button1: String = "UK company"
    val button2: String = "Charitable Incorporated Organisation (CIO)"
    val button3: String = "Limited liability partnership"
    val button4: String = "Registered society"
    val button5: String = "Scottish limited partnership"
    val button6: String = "Scottish partnership"
    val error: String = "Select the type of partner you are"
    val continue: String = "Save and continue"
    val continueLater: String = "Save and come back later"
  }

  "Lead Partner Entity Type Page" should {

    enable(SaveAndContinueLater)

    val view: partnership_business_entity_type = app.injector.instanceOf[partnership_business_entity_type]
    implicit val doc: Document = Jsoup.parse(view(LeadPartnerForm.form, isTransactor = false).body)

    disable(SaveAndContinueLater)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("50") mustBe Some(ExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("53") mustBe Some(ExpectedContent.button2)
    }

    "have the correct button3" in new ViewSetup() {
      doc.radio("52") mustBe Some(ExpectedContent.button3)
    }

    "have the correct button4" in new ViewSetup() {
      doc.radio("54") mustBe Some(ExpectedContent.button4)
    }

    "have the correct button5" in new ViewSetup() {
      doc.radio("59") mustBe Some(ExpectedContent.button5)
    }

    "have the correct button6" in new ViewSetup() {
      doc.radio("58") mustBe Some(ExpectedContent.button6)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }

    "3rd party flow has correct heading" in new ViewSetup() {
      val doc3pt: Document = Jsoup.parse(view(LeadPartnerForm.form, isTransactor = true).body)
      doc3pt.heading mustBe Some(ExpectedContent.heading3pt)
    }
  }

}
