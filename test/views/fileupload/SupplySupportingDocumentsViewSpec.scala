/*
 * Copyright 2024 HM Revenue & Customs
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

package views.fileupload

import forms.SupplySupportingDocumentsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.SupplySupportingDocuments

class SupplySupportingDocumentsViewSpec extends VatRegViewSpec {

  val taxRepPage: SupplySupportingDocuments = app.injector.instanceOf[SupplySupportingDocuments]

  lazy val view: Html = taxRepPage(SupplySupportingDocumentsForm.form)
  implicit val doc: Document = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "Do you have any supporting documents?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "These are any land or property documents that may support your VAT5L application. For example, planning permission or solicitors correspondence."
    val yes = "Yes"
    val no = "No"
    val continue = "Continue"
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

    "have the correct hint text" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedContent.para1)
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
