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

package controllers.frs

import connectors.{ConfigConnector, KeystoreConnector}
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import models.CurrentProfile
import models.view.frs.BusinessSectorView
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class RegisterForFrsWithSectorControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  val defaultBusinessSectorView: BusinessSectorView = BusinessSectorView("test", 1)

  class SetupWithBusinessSector(businessSector: BusinessSectorView = defaultBusinessSectorView) {
    val controller: RegisterForFrsWithSectorController = new RegisterForFrsWithSectorController {
      override val service: VatRegistrationService = mockVatRegistrationService
      override val configConnect: ConfigConnector = mockConfigConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector

      override def businessSectorView()(implicit headerCarrier: HeaderCarrier, profile: CurrentProfile): Future[BusinessSectorView] = {
        Future.successful(businessSector)
      }
    }

    mockAllMessages
  }

  val fakeRequest = FakeRequest(routes.RegisterForFrsWithSectorController.show())

  s"GET ${routes.RegisterForFrsWithSectorController.show()}" should {

    "return a 200 and render the page" in new SetupWithBusinessSector {
      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(controller.show()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.RegisterForFrsWithSectorController.submit()}" should {

    "return 400 with Empty data" in new SetupWithBusinessSector {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new SetupWithBusinessSector {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "true"
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/flat-rate-scheme-join-date")
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new SetupWithBusinessSector {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "false"
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }
}
