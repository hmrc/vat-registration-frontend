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
import forms.genericForms.YesOrNoFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
import models.{CurrentProfile, S4LFlatRateScheme}
import models.api.VatFlatRateScheme
import models.view.frs.JoinFrsView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class JoinFrsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  trait Setup {
    val controller: JoinFrsController = new JoinFrsController(ds, new YesOrNoFormFactory, mockS4LService, mockVatRegistrationService) {
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  val fakeRequest = FakeRequest(routes.JoinFrsController.show())

  s"GET ${routes.JoinFrsController.show()}" should {

    "render the page" when {

      "visited for the first time" in new Setup {

        mockGetCurrentProfile()

        when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(emptyVatScheme))

        callAuthorised(controller.show()) { result =>
          status(result) mustBe 200
          result includesText "Do you want to join the Flat Rate Scheme?"
        }
      }

      "user has already answered this question" in new Setup {

        mockGetCurrentProfile()

        when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(emptyVatScheme))

        callAuthorised(controller.show) { result =>
          status(result) mustBe 200
          result includesText "Do you want to join the Flat Rate Scheme?"
        }
      }

      "user's answer has already been submitted to backend" in new Setup {
        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile())))

        when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(validVatScheme))

        callAuthorised(controller.show) { result =>
          status(result) mustBe 200
          result includesText "Do you want to join the Flat Rate Scheme?"
        }
      }
    }
  }

  s"POST ${routes.JoinFrsController.submit()}" should {

    "return 400 with Empty data" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile())))

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody())(result =>
        status(result) mustBe 400
      )
    }

    "return 303 with Join Flat Rate Scheme selected Yes" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile())))

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(validVatScheme))

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
        .thenReturn(Future.successful(Some(currentProfile())))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any()))
        .thenReturn(Future.successful(VatFlatRateScheme()))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "false"
      )

      submitAuthorised(controller.submit(), request){ result =>
        result redirectsTo s"$contextRoot/check-your-answers"
      }

      verify(mockVatRegistrationService).submitVatFlatRateScheme()(any(), any())
    }
  }
}
