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

import forms.BusinessActivityDescriptionForm
import org.jsoup.Jsoup
import views.html.business_activity_description

class BusinessActivityDescriptionPageSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[business_activity_description]
  val form = BusinessActivityDescriptionForm.form
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "What does the business do?"
    val label = "Describe the type of goods or services the business sells."
    val continue = "Continue"
  }

  "The Business Activity Description page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }
    "have a textarea called description" in new ViewSetup {
      doc.textArea("description") mustBe Some(ExpectedContent.label)
    }
    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
