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

package features.turnoverEstimates

import connectors.KeystoreConnector
import forms.EstimateVatTurnoverForm.TURNOVER_ESTIMATE
import helpers.{ControllerSpec, MockMessages}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class TurnoverEstimatesControllerSpec extends ControllerSpec with MockMessages {

  trait Setup {
    val controller: TurnoverEstimatesController = new TurnoverEstimatesController {
      override val service: TurnoverEstimatesService = mockTurnoverEstimatesService
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      val authConnector: AuthConnector = mockAuthClientConnector
      val messagesApi: MessagesApi = mockMessagesAPI
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "showEstimateVatTurnover" should {

    val turnoverEstimates = TurnoverEstimates(1000L)

    "return a 200 when turnover estimates are returned from the service" in new Setup {
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(turnoverEstimates)))

      requestWithAuthorisedUser(controller.showEstimateVatTurnover, FakeRequest()){ result =>
        status(result) mustBe OK
      }
    }

    "return a 200 when turnover estimates are not returned from the service" in new Setup {
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(None))

      requestWithAuthorisedUser(controller.showEstimateVatTurnover, FakeRequest()){ result =>
        status(result) mustBe OK
      }
    }
  }

  "submitEstimateVatTurnover" should {

    val turnoverEstimates = TurnoverEstimates(1000L)

    "return a 303 and redirect when the form was submitted and saved successfully" in new Setup {
      when(mockTurnoverEstimatesService.saveTurnoverEstimates(any())(any(), any(), any()))
        .thenReturn(Future.successful(turnoverEstimates))

      val request = FakeRequest().withFormUrlEncodedBody(TURNOVER_ESTIMATE -> "1000")

      requestWithAuthorisedUser(controller.submitEstimateVatTurnover, request){ result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(features.returns.controllers.routes.ReturnsController.chargeExpectancyPage().url)
      }
    }

    "return a 400 and render the page if the form had errors" in new Setup {
      val request = FakeRequest().withFormUrlEncodedBody("test" -> "error")

      requestWithAuthorisedUser(controller.submitEstimateVatTurnover, request){ result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
