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

import forms.genericForms.YesOrNoFormFactory
import org.jsoup.Jsoup
import views.html.frs_your_flat_rate

class FrsYourFlatRatePageSpec extends VatRegViewSpec {

  val testPercentFormat = "9.5"
  val form = YesOrNoFormFactory.form()("frs.registerForWithSector")
  val view = app.injector.instanceOf[frs_your_flat_rate]
  implicit val doc = Jsoup.parse(view("", testPercentFormat,form).body)

    val heading = s"The business’s VAT flat rate is $testPercentFormat%"
    val h2 = "Do you want the business to join the Flat Rate Scheme?"
    val para1 = s"Businesses in their first year of VAT registration can take 1% off their flat rate."
    val label = "Select yes if you expect the business to regularly claim VAT refunds from HMRC"
    val continue = "Save and continue"
    val yes = "Yes"
    val no = "No"

  "The FRS Your flat rate page" must {

    "have the correct heading" in new ViewSetup {
      doc.select(Selectors.h1).text() mustBe heading
    }

    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct h2" in new ViewSetup {
      doc.select(Selectors.h2(1)).text() mustBe h2
    }

    "have the correct paragraph" in new ViewSetup {
      doc.select(Selectors.p(1)).text() mustBe para1
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