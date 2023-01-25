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

package views.applicant

import forms.RoleInTheBusinessForm
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.applicant.RoleInTheBusiness

class RoleInTheBusinessViewSpec extends VatRegViewSpec {

  object ExpectedContent {
    val name = "testName"
    val heading = s"What is $name’s role in the business?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val button1 = "Director"
    val button2 = "Company secretary"
    val button3 = "Trustee"
    val button4 = "Board Member"
    val button5 = "Other"
    val error = "Select the role within the business"
    val continue = "Save and continue"
    val continueLater = "Save and come back later"
  }

  "Role In The Business Page" should {
    val view: RoleInTheBusiness = app.injector.instanceOf[RoleInTheBusiness]
    implicit val doc: Document = Jsoup.parse(view(RoleInTheBusinessForm(UkCompany, isThirdParty = false), name = Some(ExpectedContent.name), partyType = UkCompany).body)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct radio options" in new ViewSetup() {
      doc.radio("director") mustBe Some(ExpectedContent.button1)
      doc.radio("companySecretary") mustBe Some(ExpectedContent.button2)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }

    "have additional button for trustee flow" in new ViewSetup() {
      val document: Document = Jsoup.parse(view(RoleInTheBusinessForm(Trust, isThirdParty = false), name = Some(ExpectedContent.name), partyType = Trust).body)
      document.radio("trustee") mustBe Some(ExpectedContent.button3)
      document.radio("director") mustBe Some(ExpectedContent.button1)
      document.radio("companySecretary") mustBe Some(ExpectedContent.button2)
    }

    "have two additional buttons for reg soc flow" in new ViewSetup() {
      val document: Document = Jsoup.parse(view(RoleInTheBusinessForm(RegSociety, isThirdParty = false), name = Some(ExpectedContent.name), partyType = RegSociety).body)
      document.radio("director") mustBe Some(ExpectedContent.button1)
      document.radio("companySecretary") mustBe Some(ExpectedContent.button2)
      document.radio("boardMember") mustBe Some(ExpectedContent.button4)
      document.radio("other") mustBe Some(ExpectedContent.button5)
    }

    "have two additional buttons for unincorp assoc flow" in new ViewSetup() {
      val document: Document = Jsoup.parse(view(RoleInTheBusinessForm(UnincorpAssoc, isThirdParty = false), name = Some(ExpectedContent.name), partyType = UnincorpAssoc).body)
      document.radio("director") mustBe Some(ExpectedContent.button1)
      document.radio("companySecretary") mustBe Some(ExpectedContent.button2)
      document.radio("boardMember") mustBe Some(ExpectedContent.button4)
      document.radio("other") mustBe Some(ExpectedContent.button5)
    }

    "have additional button for non uk company flow" in new ViewSetup() {
      val document: Document = Jsoup.parse(view(RoleInTheBusinessForm(NonUkNonEstablished, isThirdParty = false), name = Some(ExpectedContent.name), partyType = NonUkNonEstablished).body)
      document.radio("director") mustBe Some(ExpectedContent.button1)
      document.radio("companySecretary") mustBe Some(ExpectedContent.button2)
      document.radio("other") mustBe Some(ExpectedContent.button5)
    }
  }
}
