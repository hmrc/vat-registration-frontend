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

package views.business

import forms.BusinessEmailAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.business.BusinessEmail

class BusinessEmailViewSpec extends VatRegViewSpec {

  val viewInstance: BusinessEmail = app.injector.instanceOf[BusinessEmail]

  object ExpectedContent {
    val heading = "What is the business email address?"
    val hint = "We use this to send business communications and VAT updates."
    val para = "Full details of how we use contact information are in the HMRC Privacy Notice (opens in new tab)"
    val link = "HMRC Privacy Notice (opens in new tab)"
    val continue = "Save and continue"
  }

  "Business Email page" should {
    lazy val form = BusinessEmailAddressForm.form
    lazy val view = viewInstance(testCall, form)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct paragraphs" in new ViewSetup {
      doc.paras mustBe List(
        ExpectedContent.hint,
        ExpectedContent.para)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.link, appConfig.privacyNoticeUrl))
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
