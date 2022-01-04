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

package views

import forms.ScottishPartnershipNameForm
import org.jsoup.Jsoup
import views.html.ScottishPartnershipName

class ScottishPartnershipNameViewSpec extends VatRegViewSpec {

//  val testPartnershipName = "testPartnershipName"
  val form = ScottishPartnershipNameForm
  val view = app.injector.instanceOf[ScottishPartnershipName]
  implicit val doc = Jsoup.parse(view(form()).body)

  object ExpectedContent {
    val heading = "What is the name of the partnership?"
    val continue = "Save and continue"
  }

  "The Scottish Partnership name view" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
