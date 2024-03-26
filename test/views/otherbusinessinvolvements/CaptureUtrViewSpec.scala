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

package views.otherbusinessinvolvements

import forms.otherbusinessinvolvements.CaptureUtrForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.CaptureUtr

class CaptureUtrViewSpec extends VatRegViewSpec {
  val view: CaptureUtr = app.injector.instanceOf[CaptureUtr]

  object ExpectedContent {
    val heading = "What is the business’s Unique Taxpayer Reference?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax or Self Assessment. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val label = "What is the business’s Unique Taxpayer Reference?"
    val linkText = "I can not provide the business’s UTR number"
    val link = "https://www.tax.service.gov.uk/ask-for-copy-of-your-corporation-tax-utr"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(CaptureUtrForm(), 1).body)

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

    "have the correct para" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, ExpectedContent.link))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
