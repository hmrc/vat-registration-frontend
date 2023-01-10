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

package views.sicandcompliance

import forms.MainBusinessActivityForm
import models.api.SicCode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.sicandcompliance.MainBusinessActivity

class MainBusinessActivityViewSpec extends VatRegViewSpec {

  val view: MainBusinessActivity = app.injector.instanceOf[MainBusinessActivity]
  lazy val form = MainBusinessActivityForm.form

  val heading = "Which activity is the business’s main source of income?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val continue = "Save and continue"
  val sicCodeList = Seq(SicCode("id1", "code1", "code display 1"), SicCode("id2", "code2", "code display 2"), SicCode("id3", "code3", "code display 3"))

  implicit val doc: Document = Jsoup.parse(view(form, sicCodeList).body)

  "Main Business Activity Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "display a list of Sic Code description not pre selected" in new ViewSetup {
      lazy val document = Jsoup.parse(view(form, sicCodeList).body)

      document.getElementsByAttributeValue("name", "value").size mustBe 3
      document.getElementsByAttributeValue("checked", "checked").size mustBe 0

      document.radio("id1").isDefined mustBe true
      document.radio("id2").isDefined mustBe true
      document.radio("id3").isDefined mustBe true
    }

    "display a list of Sic Code description pre selected" in {
      val mainBusiness = SicCode("id2", "test desc", "")
      lazy val document = Jsoup.parse(view(form.fill(mainBusiness.code), sicCodeList).body)

      document.getElementsByAttributeValue("name", "value").size mustBe 3

      val preSelected = document.getElementsByAttribute("checked")
      preSelected.size mustBe 1
      preSelected.get(0).attr("value") mustBe "id2"
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}
