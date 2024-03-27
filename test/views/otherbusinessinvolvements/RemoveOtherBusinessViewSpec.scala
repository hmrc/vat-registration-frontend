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

import forms.otherbusinessinvolvements.RemoveOtherBusinessForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.RemoveOtherBusiness

class RemoveOtherBusinessViewSpec extends VatRegViewSpec {
  val view: RemoveOtherBusiness = app.injector.instanceOf[RemoveOtherBusiness]

  object ExpectedContent {
    val testOtherBusinessName = "testOtherBusinessName"
    val heading = s"Are you sure you want to remove $testOtherBusinessName?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val continue = "Save and continue"
  }

  implicit val doc: Document = Jsoup.parse(view(RemoveOtherBusinessForm(ExpectedContent.testOtherBusinessName).form, ExpectedContent.testOtherBusinessName, 1).body)

  "Remove Other Business page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
