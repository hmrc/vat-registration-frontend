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

import forms.DeclarationCapacityForm
import forms.DeclarationCapacityForm._
import org.jsoup.Jsoup
import views.html.DeclarationCapacityView

class DeclarationCapacityViewSpec extends VatRegViewSpec {
  val form = DeclarationCapacityForm()
  val view = app.injector.instanceOf[DeclarationCapacityView]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "What is your role with the business you are registering for VAT?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val radio1 = "Accountant"
    val radio2 = "Appointed representative"
    val radio3 = "Board member"
    val radio4 = "Employee authorised to complete the application"
    val radio5 = "Other"
    val continue = "Save and continue"
  }

  "The SellOrMoveNip view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title() mustBe ExpectedContent.title
    }

    "have the correct radios" in new ViewSetup {
      doc.radio(accountant) mustBe Some(ExpectedContent.radio1)
      doc.radio(representative) mustBe Some(ExpectedContent.radio2)
      doc.radio(boardMember) mustBe Some(ExpectedContent.radio3)
      doc.radio(authorisedEmployee) mustBe Some(ExpectedContent.radio4)
      doc.radio(other) mustBe Some(ExpectedContent.radio5)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
