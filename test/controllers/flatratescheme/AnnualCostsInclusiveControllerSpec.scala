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
import models.FlatRateScheme
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.FlatRateService.OverBusinessGoodsAnswer
import testHelpers.ControllerSpec
import views.html.flatratescheme.AnnualCostsInclusive

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnnualCostsInclusiveControllerSpec extends ControllerSpec with VatRegistrationFixture {

  val view: AnnualCostsInclusive = app.injector.instanceOf[AnnualCostsInclusive]

  trait Setup {
    val controller: AnnualCostsInclusiveController = new AnnualCostsInclusiveController(
      mockFlatRateService,
      mockAuthClientConnector,
      mockSessionService,
      view)

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${routes.AnnualCostsInclusiveController.show}" should {
    "return a 200 when a previously completed FlatRateScheme is returned" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }

    "return a 200 when an empty FlatRateScheme is returned from the service" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(FlatRateScheme()))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.AnnualCostsInclusiveController.submit}" should {
    val fakeRequest = FakeRequest(routes.AnnualCostsInclusiveController.submit)

    "return 400 with Empty data" in new Setup {
      val emptyRequest: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody()

      submitAuthorised(controller.submit, emptyRequest) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Annual Costs Inclusive selected Yes" in new Setup {
      when(mockFlatRateService.saveFlatRate(any[OverBusinessGoodsAnswer]())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.EstimateTotalSalesController.show.url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {
      when(mockFlatRateService.saveFlatRate(any[OverBusinessGoodsAnswer]())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.RegisterForFrsController.show.url)
      }
    }
  }

}
