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

package controllers

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import views.html.pages.error.IndividualAffinityKickOut

import scala.concurrent.ExecutionContext.Implicits.global

class IndividualAffinityKickOutControllerSpec extends ControllerSpec {

  val TestController = new IndividualAffinityKickOutController(
    app.injector.instanceOf[IndividualAffinityKickOut],
    mockAuthClientConnector,
    mockKeystoreConnector
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/individual-affinity")
  val testGetRedirectRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/individual-affinity-redirect")
  val testGetBusinessSignInRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/individual-affinity-signin")

  "show" must {
    "return an OK" in {
      val res = TestController.show(testGetRequest)

      status(res) mustBe OK
      contentType(res) mustBe Some("text/html")
      charset(res) mustBe Some("utf-8")
    }
  }

  "signOutAndRedirect" must {
    "return a SEE_OTHER with a redirect to business registration Page" in {
      val res = TestController.signOutAndRedirect(testGetRedirectRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(appConfig.individualKickoutUrl(routes.WelcomeController.show.url))
    }
  }

  "businessSignInRedirect" must {
    "return a SEE_OTHER with a redirect to business sign-in Page" in {
      val res = TestController.businessSignInRedirect(testGetBusinessSignInRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(appConfig.businessSignInLink)
    }
  }
}
