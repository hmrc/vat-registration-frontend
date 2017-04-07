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

package controllers

import helpers.VatRegSpec
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService

import scala.concurrent.Future

class WelcomeControllerSpec extends VatRegSpec {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestController extends WelcomeController(mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.WelcomeController.show())

  "GET /start" should {

    "return HTML when user is authorized to access" in {
      when(mockVatRegistrationService.createRegistrationFootprint()(any()))
        .thenReturn(Future.successful(()))

      callAuthorised(TestController.start, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }

  }

  "GET /" should {

    "redirect the user to start page" in {
      val result = TestController.show(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.WelcomeController.start().url)
    }
  }


}
