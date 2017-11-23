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
import models.S4LFlatRateScheme
import models.view.frs.AnnualCostsInclusiveView
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class AnnualCostsInclusiveControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  val fakeRequest = FakeRequest(routes.AnnualCostsInclusiveController.show())

  trait Setup {
    val controller: AnnualCostsInclusiveController = new AnnualCostsInclusiveController {
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      override val service: VatRegistrationService = mockVatRegistrationService
      override val messagesApi: MessagesApi = mockMessagesAPI
    }

    mockAllMessages
  }

  s"GET ${routes.AnnualCostsInclusiveController.show()}" should {

    "return a 200 when a previously completed S4LFlatRateScheme is returned" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      callAuthorised(controller.show()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return a 200 when an empty S4LFlatRateScheme is returned from the service" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(S4LFlatRateScheme()))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.AnnualCostsInclusiveController.submit()}" should {

    "return 400 with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val emptyRequest: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submit(), emptyRequest){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Annual Costs Inclusive selected Yes" in new Setup {

      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveAnnualCostsInclusive(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "return 303 with Annual Costs Inclusive selected within 12 months" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveAnnualCostsInclusive(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "skip next question if 2% of estimated taxable turnover <= 1K and NO answered" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.isOverLimitedCostTraderThreshold(any(), any()))
        .thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/confirm-business-type")
      }
    }

    "redirect to next question if 2% of estimated taxable turnover > 1K and NO answered" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.isOverLimitedCostTraderThreshold(any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      )

      submitAuthorised(controller.submit(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/spends-less-than-two-percent-of-turnover-a-year-on-goods")
      }
    }
  }
}
