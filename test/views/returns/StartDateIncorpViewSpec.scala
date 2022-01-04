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

import forms.VoluntaryDateForm
import models.DateSelection
import org.jsoup.Jsoup
import services.TimeService
import views.VatRegViewSpec
import views.html.returns.start_date_incorp_view

class StartDateIncorpViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[start_date_incorp_view]
  val timeService = app.injector.instanceOf[TimeService]
  val dateMin = timeService.today
  val dateMax = timeService.today
  val form = VoluntaryDateForm.form(dateMin, dateMax).fill((DateSelection.specific_date, Some(dateMin)))
  val registeredDate = timeService.dynamicFutureDateExample()
  val incorpDateAfter = true
  val dateExample = timeService.dynamicFutureDateExample()
  implicit val doc = Jsoup.parse(view(form, registeredDate, incorpDateAfter, dateExample).body)

  object ExpectedContent {
    val heading = "What do you want the business’s VAT start date to be?"
    val title = s"$heading"
    val paragraph = "You can choose the date the business becomes VAT registered when you register voluntarily."
    val panelText = "Once the business is successfully registered for VAT you cannot change its VAT start date."
    val button1 = s"The date the company was registered: $registeredDate"
    val button2 = "A different date"
    val saveAndContinue = "Save and continue"
    val hiddenPara = "This can be up to 4 years ago or within the next 3 months."
    val hiddenBullet1 = "on or after the date the business was registered"
    val hiddenBullet2 = "no more than 3 months in the future"
    val hiddenBullet3 = "no more than 4 years in the past"
    val hiddenHint = s"For example, $dateExample"
  }

  "The Start Date Incorp page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }
    "have the correct title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.title)
    }
    "have the correct paragraph text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.paragraph)
    }
    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).text mustBe ExpectedContent.panelText
    }
    "have the correct button1 text" in new ViewSetup {
      doc.radio("company_registration_date") mustBe Some(ExpectedContent.button1)
    }
    "have the correct button2 text" in new ViewSetup {
      doc.radio("specific_date") mustBe Some(ExpectedContent.button2)
    }
    "have the correct hidden paragraph text" in new ViewSetup {
      doc.select("form p").text mustBe ExpectedContent.hiddenPara
    }
    "have the correct hidden bullet 1" in new ViewSetup {
      doc.select(Selectors.bullet(1)).text mustBe ExpectedContent.hiddenBullet1
    }
    "have the correct hidden bullet 2" in new ViewSetup {
      doc.select(Selectors.bullet(2)).text mustBe ExpectedContent.hiddenBullet2
    }
    "have the correct hidden bullet 3" in new ViewSetup {
      doc.select(Selectors.bullet(3)).text mustBe ExpectedContent.hiddenBullet3
    }
    "have the correct hidden hint text" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedContent.hiddenHint)
    }
    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.saveAndContinue)
    }
  }

}
