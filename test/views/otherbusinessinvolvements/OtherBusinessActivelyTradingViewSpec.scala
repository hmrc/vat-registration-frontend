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

package views.otherbusinessinvolvements

import forms.otherbusinessinvolvements.OtherBusinessActivelyTradingForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.OtherBusinessActivelyTradingView

class OtherBusinessActivelyTradingViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[OtherBusinessActivelyTradingView]
  implicit val doc: Document = Jsoup.parse(view(OtherBusinessActivelyTradingForm.form, 1).body)

  val heading = "Is the other business still actively trading?"
  val continue = "Save and continue"
  val yes = "Yes"
  val no = "No"

  "The Other Business Actively Trading page" must {

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.select(Selectors.h1).text() mustBe heading
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(yes)
      doc.radio("false") mustBe Some(no)
    }

    "have a continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}