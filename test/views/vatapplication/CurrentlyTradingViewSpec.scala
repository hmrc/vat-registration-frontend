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

package views.vatapplication

import forms.vatapplication.CurrentlyTradingForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.vatapplication.CurrentlyTradingView

class CurrentlyTradingViewSpec extends VatRegViewSpec {

  val regDate = "21 April 2022"
  val currentlyTradingPage: CurrentlyTradingView = app.injector.instanceOf[CurrentlyTradingView]

  object PastRegDateExpectedContent {
    val heading = s"Was the business trading taxable goods or services by $regDate?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val yes = "Yes"
    val no = "No - it started trading after this date"
    val continue = "Save and continue"
  }

  object FutureRegDateExpectedContent {
    val heading = s"Will the business be trading taxable goods or services by $regDate?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val yes = "Yes"
    val no = "No - it will start trading after this date"
    val continue = "Save and continue"
  }

  "The Tax Representative page with reg start date in past" must {

    lazy val view: Html = currentlyTradingPage(CurrentlyTradingForm("past", regDate).form, "past", regDate)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(PastRegDateExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe PastRegDateExpectedContent.title
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(PastRegDateExpectedContent.yes)
      doc.radio("false") mustBe Some(PastRegDateExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(PastRegDateExpectedContent.continue)
    }
  }

  "The Tax Representative page with reg start date in future" must {

    lazy val view: Html = currentlyTradingPage(CurrentlyTradingForm("future", regDate).form, "future", regDate)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(FutureRegDateExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe FutureRegDateExpectedContent.title
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(FutureRegDateExpectedContent.yes)
      doc.radio("false") mustBe Some(FutureRegDateExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(FutureRegDateExpectedContent.continue)
    }
  }

}
