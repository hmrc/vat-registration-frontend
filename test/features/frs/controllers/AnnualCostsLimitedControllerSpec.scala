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

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import models.S4LFlatRateScheme
import play.api.test.Helpers._
import models.view.frs.AnnualCostsLimitedView
import models.view.vatFinancials.EstimateVatTurnover
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class AnnualCostsLimitedControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  trait Setup {
    val controller = new AnnualCostsLimitedController {
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      override val service: VatRegistrationService = mockVatRegistrationService
      override val messagesApi: MessagesApi = mockMessagesAPI
    }

    mockAllMessages
  }

    val fakeRequest = FakeRequest(routes.AnnualCostsLimitedController.show())
    val estimateVatTurnover = EstimateVatTurnover(1000000L)

    val s4LFlatRateSchemeNoAnnualCostsLimited: S4LFlatRateScheme = validS4LFlatRateScheme.copy(annualCostsLimited = None)

    s"GET ${routes.AnnualCostsLimitedController.show()}" should {

      "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is not found on the vat scheme" in new Setup {

        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(s4LFlatRateSchemeNoAnnualCostsLimited))

        when(mockVatRegistrationService.getFlatRateSchemeThreshold()(any(), any()))
          .thenReturn(Future.successful(1000L))

        callAuthorised(controller.show()) { result =>
          status(result) mustBe 200
          contentAsString(result) must include(MOCKED_MESSAGE)
        }
      }

      "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is found on the vat scheme" in new Setup {
        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(validS4LFlatRateScheme))

        when(mockVatRegistrationService.getFlatRateSchemeThreshold()(any(), any()))
          .thenReturn(Future.successful(1000L))

        callAuthorised(controller.show()) { result =>
          status(result) mustBe 200
          contentAsString(result) must include(MOCKED_MESSAGE)
        }
      }
    }

    s"POST ${routes.AnnualCostsLimitedController.submit()}" should {

      "return a 400 when the request is empty" in new Setup {

        mockWithCurrentProfile(Some(currentProfile))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

        submitAuthorised(controller.submit(), request){ result =>
          status(result) mustBe 400
        }
      }

      "return a 303 when AnnualCostsLimitedView.selected is Yes" in new Setup{
        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.saveAnnualCostsLimited(any())(any(), any()))
          .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES
        )

        submitAuthorised(controller.submit(), request){ result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
        }
      }

      "return 303 when AnnualCostsLimitedView.selected is Yes within 12 months" in new Setup {
        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.saveAnnualCostsLimited(any())(any(), any()))
          .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES_WITHIN_12_MONTHS
        )

        submitAuthorised(controller.submit(), request){ result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
        }
      }

      "return a 303 and redirect to confirm business sector with Annual Costs Limited selected No" in new Setup {
        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.saveAnnualCostsLimited(any())(any(), any()))
          .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

        private val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "annualCostsLimitedRadio" -> AnnualCostsLimitedView.NO
        )

        submitAuthorised(controller.submit(), request){ result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some("/register-for-vat/confirm-business-type")
        }
      }
    }
  }
