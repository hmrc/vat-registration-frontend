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

package views.business

import forms.BusinessWebsiteAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.business.BusinessWebsiteAddress

class BusinessWebsiteAddressViewSpec extends VatRegViewSpec {

  val viewInstance: BusinessWebsiteAddress = app.injector.instanceOf[BusinessWebsiteAddress]

  object ExpectedContent {
    val heading = "What is the business’ website address?"
    val continue = "Save and continue"
  }

  "Business Website Address page" should {
    lazy val form = BusinessWebsiteAddressForm.form
    lazy val view = viewInstance(testCall, form)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
