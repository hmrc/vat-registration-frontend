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
import forms.BusinessActivityDescriptionForm
import models.BusinessActivityDescription
import org.jsoup.Jsoup
import org.scalatest.Matchers
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec
import views.html.{business_activity_description => BusinessActivityDescriptionPage}

class BusinessActivityDescriptionPageSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = app.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = BusinessActivityDescriptionForm.form

  "Business Activity Description Page" should {
    "display the page not pre populated" in {
      lazy val view = BusinessActivityDescriptionPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("description").text mustBe ""
    }

    "display the page pre populated" in {
      lazy val view = BusinessActivityDescriptionPage(form.fill(BusinessActivityDescription("TEST DESCRIPTION")))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("description").text mustBe "TEST DESCRIPTION"
    }
  }
}
