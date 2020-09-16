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

import config.FrontendAppConfig
import views.html.{applicant_contact_details => ContactDetailsPage}
import forms.ContactDetailsForm
import models.view.ContactDetailsView
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec

class ContactDetailsPageSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = ContactDetailsForm.form

  "Contact Details Page" should {
    "display the page without pre populated data" in {
      lazy val view = ContactDetailsPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("email").attr("value") mustBe ""
      document.getElementById("daytimePhone").attr("value") mustBe ""
      document.getElementById("mobile").attr("value") mustBe ""
    }

    "display the page with form pre populated" in {
      val validContactDetails = ContactDetailsView(Some("1234"),Some("t@t.tt.co.tt"), Some("5678"))

      lazy val view = ContactDetailsPage(form.fill(validContactDetails))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("email").attr("value") mustBe "t@t.tt.co.tt"
      document.getElementById("daytimePhone").attr("value") mustBe "1234"
      document.getElementById("mobile").attr("value") mustBe "5678"
    }
  }
}
