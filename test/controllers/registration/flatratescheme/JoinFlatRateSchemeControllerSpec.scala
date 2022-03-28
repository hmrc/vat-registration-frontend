/*
 * Copyright 2022 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import models.api.EligibilitySubmissionData
import models.{CurrentProfile, FlatRateScheme, TurnoverEstimates}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.InternalServerException
import views.html.frs_join

import scala.concurrent.Future

class JoinFlatRateSchemeControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {

  trait Setup {
    val view = app.injector.instanceOf[frs_join]
    val controller: JoinFlatRateSchemeController = new JoinFlatRateSchemeController(
      mockFlatRateService,
      mockVatRegistrationService,
      mockAuthClientConnector,
      mockSessionService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show}" should {
    "render the page" when {
      "visited for the first time" in new Setup {
        when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
          .thenReturn(Future.successful(Some(TurnoverEstimates(150000L))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(mockFlatRateService.getFlatRate(any(), any()))
          .thenReturn(Future.successful(validFlatRate))

        when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
          .thenReturn(Future.successful(FlatRateScheme()))

        callAuthorised(controller.show) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
        }
      }

      "user has already answered this question" in new Setup {

        when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
          .thenReturn(Future.successful(Some(TurnoverEstimates(150000L))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(mockFlatRateService.getFlatRate(any(), any()))
          .thenReturn(Future.successful(validFlatRate))

        callAuthorised(controller.show) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
        }
      }
    }

    "redirect user to Summary if Turnover Estimates is more than £150K" in new Setup {
      when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(Some(TurnoverEstimates(150001L))))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.registration.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }

    "return an error if Turnover Estimates is empty" in new Setup {
      mockAuthenticated()
      when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      intercept[InternalServerException] {
        await(controller.show(FakeRequest()))
      }
    }
  }

  s"POST ${controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.submit}" should {
    val fakeRequest = FakeRequest(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.submit)

    "return 400 with Empty data" in new Setup {

      when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(("", "")))(result =>
        status(result) mustBe 400
      )
    }

    "return 303 with Join Flat Rate Scheme selected Yes" in new Setup {
      when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )
      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.FlatRateController.annualCostsInclusivePage.url)
      }
    }

    "return 303 with Join Flat Rate Scheme selected No" in new Setup {
      when(mockSessionService.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        redirectLocation(result) mustBe Some(s"$contextRoot/attachments-resolve")
      }
    }
  }

}
