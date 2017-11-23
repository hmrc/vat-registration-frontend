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

package controllers.frs

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class RegisterForFrsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  trait Setup {
    val controller: RegisterForFrsController = new RegisterForFrsController {
      override val service: VatRegistrationService = mockVatRegistrationService
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }

    mockAllMessages
  }

  val fakeRequest = FakeRequest(routes.RegisterForFrsController.show())

  s"GET ${routes.RegisterForFrsController.show()}" should {

    "return a 200 and render the page" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(controller.show()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.RegisterForFrsController.submit()}" should {

    "return 400 with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "true"
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/flat-rate-scheme-join-date")
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.saveFRSStartDate(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "false"
      )

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }
}
