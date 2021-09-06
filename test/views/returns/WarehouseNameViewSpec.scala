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

package views.returns

import forms.WarehouseNameForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.VatRegViewSpec
import views.html.returns.WarehouseNameView

class WarehouseNameViewSpec extends VatRegViewSpec {

  val form: Form[String] = WarehouseNameForm.form
  val view: WarehouseNameView = app.injector.instanceOf[WarehouseNameView]
  implicit val doc: Document = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "What is the business name of the Fulfilment Warehouse?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val continue = "Save and continue"
  }

  "Warehouse number page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
