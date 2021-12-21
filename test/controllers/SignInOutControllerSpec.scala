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

package controllers

import controllers.callbacks.SignInOutController
import play.api.mvc.Session
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.pages.error.TimeoutView

class SignInOutControllerSpec extends ControllerSpec with FutureAssertions {

  val testController: SignInOutController = new SignInOutController(
    mockAuthClientConnector,
    mockSessionService,
    app.injector.instanceOf[TimeoutView]
  )

  "Post-sign-in" should {
    "redirect to CT post sign in" in {
      callAuthorised(testController.postSignIn) { res =>
        redirectLocation(res) mustBe Some(controllers.routes.WelcomeController.show.url)
      }
    }
  }

  "signOut" should {
    "redirect to the exit questionnaire and clear the session" in {
      callAuthorisedOrg(testController.signOut) { res =>
        redirectLocation(res) mustBe Some(appConfig.feedbackUrl)
        session(res) mustBe Session.emptyCookie
      }
    }
  }

  "renewSession" should {
    "return 200 when hit with Authorised User" in {
      callAuthorisedOrg(testController.renewSession()) { a =>
        status(a) mustBe 200
        contentType(a) mustBe Some("image/jpeg")
      }
    }
  }

  "destroySession" should {
    "return redirect to timeout show and get rid of headers" in {
      val fr = FakeRequest().withHeaders(("playFoo", "no more"))

      val res = testController.destroySession()(fr)
      status(res) mustBe 303
      headers(res).contains("playFoo") mustBe false

      redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.timeoutShow().url)
    }
  }

  "timeoutShow" should {
    "return 200" in {
      val res = testController.timeoutShow()(FakeRequest())
      status(res) mustBe 200
    }
  }
}
