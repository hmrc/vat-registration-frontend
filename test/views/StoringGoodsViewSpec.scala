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

import forms.StoringGoodsForm
import org.jsoup.Jsoup
import views.html.StoringGoods

class StoringGoodsViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[StoringGoods]
  val form = app.injector.instanceOf[StoringGoodsForm].form

  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedMessages {
    val title = "Where is the business storing goods for dispatch?"
    val h1 = "Where is the business storing goods for dispatch?"
    val error = "Select the place the business stores goods for dispatch"
    val ukRadio = "Within the United Kingdom"
    val overseasRadio = "Overseas"
    val continue = "Save and continue"
  }

  "the WarehouseLocation view" should {
    "have the correct title" in new ViewSetup {
      doc.title must include (ExpectedMessages.title)
    }
    "have the correct H1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.h1)
    }
    "have the correct two radio options" in new ViewSetup {
      doc.radio("uk") mustBe Some(ExpectedMessages.ukRadio)
      doc.radio("overseas") mustBe Some(ExpectedMessages.overseasRadio)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.continue)
    }
  }

}
