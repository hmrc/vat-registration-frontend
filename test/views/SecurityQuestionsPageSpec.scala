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
import views.html.{officer_security_questions => SecurityQuestionsPage}
import forms.SecurityQuestionsForm
import models.view.SecurityQuestionsView
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec

class SecurityQuestionsPageSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = SecurityQuestionsForm.form

  "Security Questions Page" should {
    "display the page without pre populated data" in {
      lazy val view = SecurityQuestionsPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("dob.day").attr("value") mustBe ""
      document.getElementById("dob.month").attr("value") mustBe ""
      document.getElementById("dob.year").attr("value") mustBe ""
    }

    "display the page with form pre populated" in {
      val validOfficerSecurityQuestions = SecurityQuestionsView(LocalDate.of(1998, 7, 12))

      lazy val view = SecurityQuestionsPage(form.fill(validOfficerSecurityQuestions))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("dob.day").attr("value") mustBe "12"
      document.getElementById("dob.month").attr("value") mustBe "7"
      document.getElementById("dob.year").attr("value") mustBe "1998"
    }
  }
}
