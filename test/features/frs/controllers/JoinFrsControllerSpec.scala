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
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import helpers.{ControllerSpec, MockMessages}
import models.{CurrentProfile, S4LFlatRateScheme}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class JoinFrsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  trait Setup {
    val controller: JoinFrsController = new JoinFrsController{
      override val form: Form[YesOrNoAnswer] = (new YesOrNoFormFactory).form("joinFrs")("frs.join")
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      override val service: VatRegistrationService = mockVatRegistrationService
      override val messagesApi: MessagesApi = mockMessagesAPI
    }
  }

  val fakeRequest = FakeRequest(routes.JoinFrsController.show())

  mockAllMessages

  s"GET ${routes.JoinFrsController.show()}" should {

    "render the page" when {

      "visited for the first time" in new Setup {

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(S4LFlatRateScheme()))

        callAuthorised(controller.show()) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }

      "user has already answered this question" in new Setup {

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(validS4LFlatRateScheme))

        callAuthorised(controller.show) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }
    }
  }

  s"POST ${routes.JoinFrsController.submit()}" should {

    "return 400 with Empty data" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody())(result =>
        status(result) mustBe 400
      )
    }

    "return 303 with Join Flat Rate Scheme selected Yes" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockVatRegistrationService.saveJoinFRS(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "true"
      )
      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(s"$contextRoot/spends-less-including-vat-on-goods")
      }
    }

    "return 303 with Join Flat Rate Scheme selected No" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockVatRegistrationService.saveJoinFRS(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "false"
      )

      submitAuthorised(controller.submit(), request){ result =>
        redirectLocation(result) mustBe Some(s"$contextRoot/check-your-answers")
      }
    }
  }
}
