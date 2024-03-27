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

package views.vatapplication

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.MessageDateFormat
import views.VatRegViewSpec
import views.html.vatapplication.MandatoryStartDateNoChoiceView

import java.time.LocalDate

class MandatoryStartDateNoChoiceViewSpec extends VatRegViewSpec {

  val view: MandatoryStartDateNoChoiceView = app.injector.instanceOf[MandatoryStartDateNoChoiceView]
  val calculatedDate: LocalDate = LocalDate.now()
  val formattedDate: String = MessageDateFormat.format(calculatedDate)

  implicit val doc: Document = Jsoup.parse(view(formattedDate).body)

  object ExpectedMessages {
    val heading = "VAT registration start date"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = s"The business’s expected VAT registration start date is $formattedDate."
    val para2 = "This could change but we will confirm the date by letter."
  }

  "the Mandatory Start Date No Choice page" must {
    "have a backlink" in new ViewSetup {
      doc.hasBackLink mustBe true
    }
    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct paras" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.para1)
      doc.para(2) mustBe Some(ExpectedMessages.para2)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton.isDefined mustBe true
    }
  }

}
