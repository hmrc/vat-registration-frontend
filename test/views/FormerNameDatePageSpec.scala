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

import java.time.LocalDate

import config.FrontendAppConfig
import forms.FormerNameDateForm
import models.view.FormerNameDateView
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec
import views.html.{former_name_date => FormerNameDatePage}

class FormerNameDatePageSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector: Injector = fakeApplication.injector
  implicit val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = FormerNameDateForm.form

  "Former Name Date Page" should {
    "display the page without pre populated data" in {
      lazy val view = FormerNameDatePage(form, "Test Old Name")
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("formerNameDate.day").attr("value") mustBe ""
      document.getElementById("formerNameDate.month").attr("value") mustBe ""
      document.getElementById("formerNameDate.year").attr("value") mustBe ""
    }

    "display the page with form pre populated" in {
      val validFormerNameDate = FormerNameDateView(LocalDate.of(1998, 7, 12))

      lazy val view = FormerNameDatePage(form.fill(validFormerNameDate), "Test Old Name")
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("formerNameDate.day").attr("value") mustBe "12"
      document.getElementById("formerNameDate.month").attr("value") mustBe "7"
      document.getElementById("formerNameDate.year").attr("value") mustBe "1998"
    }
  }
}
