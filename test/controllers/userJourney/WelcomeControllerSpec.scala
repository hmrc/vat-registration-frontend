/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.userJourney


import builders.AuthBuilder
import controllers.CommonPlayDependencies
import helpers.VatRegSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._


class WelcomeControllerSpec extends VatRegSpec {

  class Setup(ds: CommonPlayDependencies) {

    object TestController extends WelcomeController(ds) {
      override val authConnector = mockAuthConnector
    }

  }

  "GET /start" should {
    "redirect to GG sign in when not authorized" in {
      val controller = new WelcomeController(ds)
      AuthBuilder.showWithUnauthorisedUser(controller.start) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(authUrl)
      }
    }

    "return HTML when user is authorized to access" in new Setup(ds) {
      AuthBuilder.showWithAuthorisedUser(TestController.start, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
  }

  "GET /" should {
    "redirect the user to start page" in {
      val controller = new WelcomeController(ds)
      val result = controller.show(FakeRequest("GET", "/"))
      status(result) mustBe SEE_OTHER
      inside(redirectLocation(result)) {
        case Some(redirectUri) => redirectUri mustBe routes.WelcomeController.start().toString
      }
    }
  }


}

