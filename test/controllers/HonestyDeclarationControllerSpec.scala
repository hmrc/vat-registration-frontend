/*
 * Copyright 2024 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.ControllerSpec
import views.html.HonestyDeclaration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HonestyDeclarationControllerSpec extends ControllerSpec with MockVatRegistrationService with VatRegistrationFixture {

  val TestController = new HonestyDeclarationController(
    app.injector.instanceOf[HonestyDeclaration],
    mockAuthClientConnector,
    mockSessionService,
    vatRegistrationServiceMock
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/honesty-declaration")
  val testPostRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/honesty-declaration")

  "show" must {
    "return an OK" in {
      val res = TestController.show(testGetRequest)

      status(res) mustBe OK
      contentType(res) mustBe Some("text/html")
      charset(res) mustBe Some("utf-8")
    }
  }

  "submit" must {
    s"redirect to Part Of Organisation Page if user is transactor" in {
      mockUpsertSection(regId, data = true)(Future.successful(true))

      val res = TestController.submit(testPostRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(appConfig.eligibilityStartUrl(testRegId))
    }
  }
}
