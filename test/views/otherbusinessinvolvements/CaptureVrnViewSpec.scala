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

package views.otherbusinessinvolvements

import forms.otherbusinessinvolvements.CaptureVrnForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.CaptureVrn

class CaptureVrnViewSpec extends VatRegViewSpec {
  val view: CaptureVrn = app.injector.instanceOf[CaptureVrn]

  object ExpectedContent {
    val heading = "What is the other business’s VAT number?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val hint = "This is 9 numbers, for example 123456789. You can find it on the businesses VAT registration certificate."
    val label = "What is the name of the other business?"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(CaptureVrnForm(), 1).body)

  "Capture VRN page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct hint" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedContent.hint)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
