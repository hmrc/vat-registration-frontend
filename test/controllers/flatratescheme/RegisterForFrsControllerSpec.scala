/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.flatratescheme

import fixtures.VatRegistrationFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import views.html.flatratescheme.RegisterForFrs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegisterForFrsControllerSpec extends ControllerSpec with VatRegistrationFixture {

  val view: RegisterForFrs = app.injector.instanceOf[RegisterForFrs]

  trait Setup {
    val controller: RegisterForFrsController = new RegisterForFrsController(
      mockFlatRateService,
      mockAuthClientConnector,
      mockSessionService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }


  s"GET ${routes.RegisterForFrsController.show}" should {
    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.RegisterForFrsController.submit}" should {
    val fakeRequest = FakeRequest(routes.RegisterForFrsController.submit)

    "return 400 with Empty data" in new Setup {

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody()

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {
      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.StartDateController.show.url)
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {
      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to task list when selected No" in new Setup {
      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/application-progress")
      }
    }
  }

}
