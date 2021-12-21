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

package views.returns

import forms.AnnualStaggerForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.returns.last_month_of_accounting_year

class LastMonthOfAccountingYearViewSpec extends VatRegViewSpec {

  lazy val title = s"$header - Register for VAT - GOV.UK"
  lazy val header = "What will be the last month of your accounting year?"
  lazy val text = "As part of the Annual Accounting Scheme your VAT Return will be due two months after the last month of your accounting year."
  lazy val radio1 = "January"
  lazy val radio2 = "February"
  lazy val radio3 = "March"
  lazy val radio4 = "April"
  lazy val radio5 = "May"
  lazy val radio6 = "June"
  lazy val radio7 = "July"
  lazy val radio8 = "August"
  lazy val radio9 = "September"
  lazy val radio10 = "October"
  lazy val radio11 = "November"
  lazy val radio12 = "December"
  lazy val buttonText = "Save and continue"

  "Honesty Declaration Page" must {

    val view = app.injector.instanceOf[last_month_of_accounting_year].apply(AnnualStaggerForm.form)

    val doc = Jsoup.parse(view.body)

    "have the right title" in {
      doc.title() mustBe title
    }

    "have the right header" in {
      doc.select(Selectors.h1).text() mustBe header
    }

    "have the right text" in {
      doc.select(Selectors.p(1)).text() mustBe text
    }

    "have the right button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

    "have the right radio buttons" in {
      doc.select(Selectors.radio(1)).text() mustBe radio1
      doc.select(Selectors.radio(2)).text() mustBe radio2
      doc.select(Selectors.radio(3)).text() mustBe radio3
      doc.select(Selectors.radio(4)).text() mustBe radio4
      doc.select(Selectors.radio(5)).text() mustBe radio5
      doc.select(Selectors.radio(6)).text() mustBe radio6
      doc.select(Selectors.radio(7)).text() mustBe radio7
      doc.select(Selectors.radio(8)).text() mustBe radio8
      doc.select(Selectors.radio(9)).text() mustBe radio9
      doc.select(Selectors.radio(10)).text() mustBe radio10
      doc.select(Selectors.radio(11)).text() mustBe radio11
      doc.select(Selectors.radio(12)).text() mustBe radio12
    }
  }
}