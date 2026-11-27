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

import forms.SupportingDocumentsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.SupportingDocuments

class SupportingDocumentsViewSpec extends VatRegViewSpec {

  val supportingDocumentsPage: SupportingDocuments = app.injector.instanceOf[SupportingDocuments]

  lazy val view: Html = supportingDocumentsPage(SupportingDocumentsForm.form)
  implicit val doc: Document = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "Supporting documents for the VAT5L"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "Supporting documents are optional. However, sending them now may save delays to your application if we decide we need to ask for them later."
    val para2 = "Supporting documents for the VAT5L are any land or property documents that may support your application such as planning permission or solicitors correspondence."
    val legend = "Do you want to upload any supporting documents for the VAT5L?"
    val linkText = "Find out more about supporting documents for VAT5L (opens in new tab)"
    val yes = "Yes"
    val no = "No"
    val continue = "Continue"
  }

  "The Supporting Documents page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "link to the correct guidance" in new ViewSetup {
      doc.select(s"""main a[href="${appConfig.supportingDocuments}"]""").text mustBe ExpectedContent.linkText
    }

    "have the question as the radio legend" in new ViewSetup {
      doc.legend(1) mustBe Some(ExpectedContent.legend)
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }

    "post to the SupportingDocumentsController" in new ViewSetup {
      doc.select("form").attr("action") mustBe controllers.fileupload.routes.SupportingDocumentsController.submit.url
    }
  }

}
