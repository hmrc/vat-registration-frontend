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

import forms.VAT1614AForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.fileupload.VAT1614A

class VAT1614AViewSpec extends VatRegViewSpec {

  val taxRepPage: VAT1614A = app.injector.instanceOf[VAT1614A]

  lazy val view: Html = taxRepPage(VAT1614AForm.form)
  implicit val doc: Document = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "VAT1614A form"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "Complete and upload this form to notify HMRC of your decision to opt to tax land and or buildings."
    val legend = "Do you need to upload a VAT1614A?"
    val linkText = "Download and find out more about VAT1614A forms (opens in new tab)"
    val yes = "Yes"
    val no = "No"
    val continue = "Continue"
  }

  "The VAT1614A page" must {
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
    }

    "link to the correct guidance" in new ViewSetup {
      doc.select(s"""main a[href="${appConfig.vat1614ALink}"]""").text mustBe ExpectedContent.linkText
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

    "post to the VAT1614AController" in new ViewSetup {
      doc.select("form").attr("action") mustBe controllers.fileupload.routes.VAT1614AController.submit.url
    }
  }

}
