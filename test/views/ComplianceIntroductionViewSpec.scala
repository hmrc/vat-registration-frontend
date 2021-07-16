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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.compliance_introduction

class ComplianceIntroductionViewSpec extends VatRegViewSpec {
  object ExpectedContent {
    val heading  = "Tell us more about your business"
    val title    = s"$heading - Register for VAT - GOV.UK"
    val para     = "We’re going to ask you a few questions about your business activities so we can understand what the business does."
    val continue = "Save and continue"
  }

  val view: compliance_introduction = app.injector.instanceOf[compliance_introduction]

  implicit val doc: Document = Jsoup.parse(view().body)

  "Compliance Introduction page" must {
    "has back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "has the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "has the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "has correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "has a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
