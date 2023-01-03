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

package views.vatapplication

import forms.TaxRepForm
import org.jsoup.Jsoup
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.vatapplication.TaxRepresentative

class TaxRepresentativeViewSpec extends VatRegViewSpec {

  val taxRepPage: TaxRepresentative = app.injector.instanceOf[TaxRepresentative]

  lazy val view: Html = taxRepPage(TaxRepForm.form)
  implicit val doc = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "Do you want to appoint a UK tax representative for the business?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "You can appoint a tax representative to deal with VAT matters on behalf of the business."
    val para2 = "They will be the point of contact for VAT and they can be made responsible for any of the business’s VAT debts."
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "The Tax Representative page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
