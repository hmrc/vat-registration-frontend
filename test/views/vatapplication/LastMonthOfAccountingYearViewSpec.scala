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

import forms.AnnualStaggerForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.vatapplication.LastMonthOfAccountingYear

class LastMonthOfAccountingYearViewSpec extends VatRegViewSpec {

  lazy val title = s"$header - Register for VAT - GOV.UK"
  lazy val header = "What will be the last month of the business’s accounting year?"
  lazy val text = "As part of the Annual Accounting Scheme the VAT Return will be due two months after the last month of the business’s accounting year."
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

    val view = app.injector.instanceOf[LastMonthOfAccountingYear].apply(AnnualStaggerForm.form)

    implicit val doc = Jsoup.parse(view.body)

    "have the right title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the right header" in new ViewSetup {
      doc.heading mustBe Some(header)
    }

    "have the right text" in new ViewSetup {
      doc.para(1) mustBe Some(text)
    }

    "have the right button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }

    "have the right radio buttons" in new ViewSetup {
      doc.radio("january") mustBe Some(radio1)
      doc.radio("february") mustBe Some(radio2)
      doc.radio("march") mustBe Some(radio3)
      doc.radio("april") mustBe Some(radio4)
      doc.radio("may") mustBe Some(radio5)
      doc.radio("june") mustBe Some(radio6)
      doc.radio("july") mustBe Some(radio7)
      doc.radio("august") mustBe Some(radio8)
      doc.radio("september") mustBe Some(radio9)
      doc.radio("october") mustBe Some(radio10)
      doc.radio("november") mustBe Some(radio11)
      doc.radio("december") mustBe Some(radio12)
    }
  }
}