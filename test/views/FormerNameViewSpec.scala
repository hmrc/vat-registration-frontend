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

import config.FrontendAppConfig
import views.html.{former_name => FormerNamePage}
import forms.FormerNameForm
import models.view.FormerNameView
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec

class FormerNameViewSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = FormerNameForm.form

  "Former Name Page" should {
    "display the page without pre populated data" in {
      lazy val view = FormerNamePage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "formerNameRadio").size mustBe 2
      document.getElementsByAttributeValue("checked", "checked").size mustBe 0
      document.getElementById("formerName").attr("value") mustBe ""
    }

    "display the page with form pre populated" in {
      val validFormerName = FormerNameView(true, Some("Test Old Name"))

      lazy val view = FormerNamePage(form.fill(validFormerName))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("formerNameRadio-true").attr("checked") mustBe "checked"
      document.getElementById("formerName").attr("value") mustBe "Test Old Name"
    }
  }
}
