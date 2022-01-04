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

package views.transactor

import forms.TransactorTelephoneForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.transactor.TelephoneNumber

class TelephoneNumberViewSpec extends VatRegViewSpec {
  val view: TelephoneNumber = app.injector.instanceOf[TelephoneNumber]
  implicit val doc: Document = Jsoup.parse(view(TransactorTelephoneForm.form).body)

  object ExpectedContent {
    val heading = "What is your telephone number?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val continue = "Save and continue"
  }

  "Transactor telephone number page" should {
    "have back link" in new ViewSetup {
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
