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
import forms.RoleInTheBusinessForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.applicant.role_in_the_business

class RoleInTheBusinessViewSpec extends VatRegViewSpec with FeatureSwitching {

  implicit val errorKey: String = "pages.leadPartnerEntityType.missing"

  object ExpectedContent {
    val name: String = "testName"
    val heading: String = s"What is $name’s role in the business?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button1: String = "Director"
    val button2: String = "Company secretary"
    val button3: String = "Trustee"
    val error: String = "Select the role within the business"
    val continue: String = "Save and continue"
    val continueLater: String = "Save and come back later"
  }

  "Role In The Business Page" should {

    enable(SaveAndContinueLater)

    val view: role_in_the_business = app.injector.instanceOf[role_in_the_business]
    implicit val doc: Document = Jsoup.parse(view(RoleInTheBusinessForm(), name = Some(ExpectedContent.name), isTrust = false).body)

    disable(SaveAndContinueLater)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("director") mustBe Some(ExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("companySecretary") mustBe Some(ExpectedContent.button2)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }

    "have additional button for trustee flow" in new ViewSetup() {
      val docTrustee: Document = Jsoup.parse(view(RoleInTheBusinessForm(), name = Some(ExpectedContent.name), isTrust = true).body)
      docTrustee.radio("trustee") mustBe Some(ExpectedContent.button3)
    }
  }

}
