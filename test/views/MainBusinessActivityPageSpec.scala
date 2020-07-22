/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.MainBusinessActivityForm
import models.MainBusinessActivityView
import models.api.SicCode
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.{main_business_activity => MainBusinessActivityPage}

class MainBusinessActivityPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  lazy val form = MainBusinessActivityForm.form

  val sicCodeList = Seq(SicCode("id1", "code1", "code display 1"), SicCode("id2", "code2", "code display 2"), SicCode("id3", "code3", "code display 3"))

  "Main Business Activity Page" should {
    "display a list of Sic Code description not pre selected" in {
      lazy val view = MainBusinessActivityPage(form, sicCodeList)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "mainBusinessActivityRadio").size shouldBe 3
      document.getElementsByAttributeValue("checked", "checked").size shouldBe 0

      document.getElementById("mainBusinessActivityRadio-id1").attr("value") shouldBe "id1"
      document.getElementById("mainBusinessActivityRadio-id2").attr("value") shouldBe "id2"
      document.getElementById("mainBusinessActivityRadio-id3").attr("value") shouldBe "id3"
    }

    "display a list of Sic Code description pre selected" in {
      val mainBus = MainBusinessActivityView("id2", None)

      lazy val view = MainBusinessActivityPage(form.fill(mainBus), sicCodeList)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "mainBusinessActivityRadio").size shouldBe 3

      val preSelected = document.getElementsByAttributeValue("checked", "checked")
      preSelected.size shouldBe 1
      preSelected.get(0).attr("value") shouldBe "id2"
    }
  }
}
