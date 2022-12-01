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

import fixtures.VatRegistrationFixture
import forms.PreviousAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.applicant.previous_address

class PreviousAddressViewSpec extends VatRegViewSpec with VatRegistrationFixture {

  val previousAddressPage: previous_address = app.injector.instanceOf[previous_address]

  val name = "testFirstName"
  lazy val form: Form[Boolean] = PreviousAddressForm.form()
  lazy val nonTransactorView: Html = previousAddressPage(form, None, testAddress)
  implicit val nonTransactorDoc: Document = Jsoup.parse(nonTransactorView.body)
  lazy val transactorView: Html = previousAddressPage(form, Some(name), testAddress)
  val transactorDoc: Document = Jsoup.parse(transactorView.body)

  val heading = "Have you lived at this home address for 3 years or more?"
  val namedHeading = "Has testFirstName lived at this home address for 3 years or more?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val address = "Line1 Line2 AA11AA United Kingdom"
  val yes = "Yes"
  val no = "No"
  val continue = "Save and continue"

  "Previous Address Page" should {
    "display the page without pre populated data" in {
      nonTransactorDoc.getElementsByAttributeValue("name", "value").size mustBe 2
      nonTransactorDoc.getElementsByAttribute("checked").size mustBe 0
    }

    "display the page with form pre populated" in {
      lazy val view = previousAddressPage(form.fill(true), None, testAddress)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "value").size mustBe 2
      document.getElementsByAttribute("checked").size mustBe 1
    }

    "have a back link" in new ViewSetup {
      nonTransactorDoc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      nonTransactorDoc.heading mustBe Some(heading)
    }

    "have the correct heading when the user is a transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe namedHeading
    }

    "have the correct page title" in new ViewSetup {
      nonTransactorDoc.title mustBe title
    }

    "have the correct address displayed" in new ViewSetup {
      nonTransactorDoc.getElementById("address").text() mustBe address
    }

    "have yes/no radio options" in new ViewSetup {
      nonTransactorDoc.radio("true") mustBe Some(yes)
      nonTransactorDoc.radio("false") mustBe Some(no)
    }

    "have a save and continue button" in new ViewSetup {
      nonTransactorDoc.submitButton mustBe Some(continue)
    }
  }
}
