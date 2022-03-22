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

import forms.IntermediarySupplyForm
import org.jsoup.Jsoup
import views.html.labour.intermediary_supply

class IntermediarySupplyViewSpec extends VatRegViewSpec {

  val testTransactorName = Some("John")

  val view = app.injector.instanceOf[intermediary_supply]
  val transactorDoc = Jsoup.parse(view(IntermediarySupplyForm(testTransactorName).form, testTransactorName).body)
  implicit val doc = Jsoup.parse(view(IntermediarySupplyForm(None).form, None).body)

  object ExpectedContent {
    val title = "Are you an intermediary arranging the supply of workers? - Register for VAT - GOV.UK"
    val heading = "Are you an intermediary arranging the supply of workers?"
    val thirdPartyHeading = "Is " + testTransactorName.get + " an intermediary arranging the supply of workers?"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "Intermediary Supply Page" should {
    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }
    "have the correct heading when the user is not transactor" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }
    "have the correct heading when the user is transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe ExpectedContent.thirdPartyHeading
    }
    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }
    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }
}
