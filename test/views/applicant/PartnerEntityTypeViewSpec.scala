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

import forms.PartnerForm
import models.Entity.leadEntityIndex
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.applicant.PartnerEntityType

class PartnerEntityTypeViewSpec extends VatRegViewSpec {

  implicit val errorKey: String = "pages.leadPartnerEntityType.missing"

  object GlobalExpectedContent {
    val button1: String = "An actual person"
    val button2: String = "A business"
    val continue: String = "Save and continue"
    val continueLater: String = "Save and come back later"
    val panelText: String = "A partner in a partnership does not have to be an actual person. For example, a limited company can be a partner."
  }
  object LeadPartnerExpectedContent {
    val heading: String = "What type of partner are you?"
    val heading3pt: String = "What type of partner is the lead partner?"
    val error: String = "Select the type of partner you are"
    val title = s"$heading - Register for VAT - GOV.UK"
  }

  object AdditionalPartnerExpectedContent {
    val heading: String = s"What type of partner is the second partner?"
    val error: String = "Select the partner type"
    val title: String = s"$heading - Register for VAT - GOV.UK"
  }

  "Partner Entity Type Page for lead partner" should {

    val view: PartnerEntityType = app.injector.instanceOf[PartnerEntityType]
    implicit val doc: Document = Jsoup.parse(view(PartnerForm.form, isTransactor = false, index = leadEntityIndex).body)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe LeadPartnerExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(LeadPartnerExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("Z1") mustBe Some(GlobalExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("BusinessEntity") mustBe Some(GlobalExpectedContent.button2)
    }

    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe GlobalExpectedContent.panelText
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe GlobalExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe GlobalExpectedContent.continueLater
    }

    "3rd party flow has correct heading" in new ViewSetup() {
      val doc3pt: Document = Jsoup.parse(view(PartnerForm.form, isTransactor = true, index = leadEntityIndex).body)
      doc3pt.heading mustBe Some(LeadPartnerExpectedContent.heading3pt)
    }
  }

  "Partner Entity Type Page for additional partners" should {

    val partnerIndex = 2
    val view: PartnerEntityType = app.injector.instanceOf[PartnerEntityType]
    implicit val doc: Document = Jsoup.parse(view(PartnerForm.form, isTransactor = true, index = partnerIndex).body)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe AdditionalPartnerExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(AdditionalPartnerExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("Z1") mustBe Some(GlobalExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("BusinessEntity") mustBe Some(GlobalExpectedContent.button2)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe GlobalExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe GlobalExpectedContent.continueLater
    }
  }

}