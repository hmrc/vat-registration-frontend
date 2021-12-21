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

package views.transactor

import forms.PartOfOrganisationForm
import forms.genericForms.YesOrNoFormFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.transactor.PartOfOrganisationView

class PartOfOrganisationViewSpec extends VatRegViewSpec {

  val form = YesOrNoFormFactory.form()("transactor.partOfOrganisation")
  val view = app.injector.instanceOf[PartOfOrganisationView]
  implicit val doc: Document = Jsoup.parse(view(PartOfOrganisationForm.form).body)

  val heading = "Are you a part of an organisation?"
  val indentText = "As you are registering on behalf of someone else we need to collect your details in order to know a bit more about you."
  val continue = "Save and continue"
  val yes = "Yes"
  val no = "No"

  "The Part Of Organisation page" must {

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.select(Selectors.h1).text() mustBe heading
    }
    "have the correct indent text" in new ViewSetup {
      doc.select(Selectors.indent).first().text() mustBe indentText
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