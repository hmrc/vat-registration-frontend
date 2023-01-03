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

package views.flatratescheme

import fixtures.FlatRateFixtures
import forms.ChooseBusinessTypeForm
import models.{FrsBusinessType, FrsGroup}
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.flatratescheme.choose_business_type

class ChooseBusinessTypeViewSpec extends VatRegViewSpec with FlatRateFixtures {

  val view = app.injector.instanceOf[choose_business_type]
  val groupings = Seq(
    FrsGroup(
      label = "section",
      labelCy = "sectionCy",
      categories = List(FrsBusinessType(businessCategory, "testCategory", "testCategoryCy", 10))
    )
  )
  implicit val document = Jsoup.parse(view(ChooseBusinessTypeForm.form(Seq()), groupings).body)

  "Choose Business Type view" must {
    "have the correct title" in new ViewSetup {
      doc.title must include("Choose the main business type")
    }
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some("Choose the main business type")
    }
    "have a legend for each section" in new ViewSetup {
      doc.select(doc.legend(1)).text mustBe "section"
    }
    "create radio buttons for each category" in new ViewSetup {
      doc.radio("019") mustBe Some("testCategory")
    }
    "have a continue button" in new ViewSetup {
      doc.submitButton mustBe Some("Save and continue")
    }
  }

}
