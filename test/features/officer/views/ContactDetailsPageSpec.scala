/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.views

import features.officer.forms.ContactDetailsForm
import features.officer.models.view.ContactDetailsView
import features.officer.views.html.{officer_contact_details => ContactDetailsPage}
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ContactDetailsPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  lazy val form = ContactDetailsForm.form

  "Contact Details Page" should {
    "display the page without pre populated data" in {
      lazy val view = ContactDetailsPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("email").attr("value") shouldBe ""
      document.getElementById("daytimePhone").attr("value") shouldBe ""
      document.getElementById("mobile").attr("value") shouldBe ""
    }

    "display the page with form pre populated" in {
      val validContactDetails = ContactDetailsView(Some("t@t.tt.co.tt"), Some("1234"), Some("5678"))

      lazy val view = ContactDetailsPage(form.fill(validContactDetails))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("email").attr("value") shouldBe "t@t.tt.co.tt"
      document.getElementById("daytimePhone").attr("value") shouldBe "1234"
      document.getElementById("mobile").attr("value") shouldBe "5678"
    }
  }
}
