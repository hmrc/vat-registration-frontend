/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.flatratescheme

import controllers.routes
import fixtures.FlatRateFixtures
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import views.html.frs_confirm_business_sector

import java.util.MissingResourceException
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ConfirmBusinessTypeControllerSpec extends ControllerSpec with FlatRateFixtures {

  trait Setup {
    val view = app.injector.instanceOf[frs_confirm_business_sector]
    val controller: ConfirmBusinessTypeController = new ConfirmBusinessTypeController(
      mockAuthClientConnector,
      mockSessionService,
      mockFlatRateService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"show" should {
    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }

    "redirect to choose business type page if there's no match of the business type against main business activity" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.failed(new MissingResourceException(s"Missing Business Type for id: testId", "ConfigConnector", "id")))

      callAuthorised(controller.show) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.registration.flatratescheme.routes.ChooseBusinessTypeController.show.url)
      }
    }
  }

  s"submit" should {
    val fakeRequest = FakeRequest(controllers.registration.flatratescheme.routes.ConfirmBusinessTypeController.submit)

    "works with Empty data" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveConfirmSector(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/confirm-flat-rate")
      }
    }
  }

}
