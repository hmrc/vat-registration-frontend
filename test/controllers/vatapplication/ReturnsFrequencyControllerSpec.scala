/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.vatapplication

import fixtures.VatRegistrationFixture
import forms.vatapplication.ReturnsFrequencyForm
import models.CurrentProfile
import models.api.{Individual, NETP}
import models.api.vatapplication._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.vatapplication.ReturnFrequency

import scala.concurrent.Future

class ReturnsFrequencyControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {

  class Setup() {
    val returnFrequencyView: ReturnFrequency = app.injector.instanceOf[ReturnFrequency]
    val testController = new ReturnsFrequencyController(
      mockSessionService, mockAuthClientConnector, movkVatApplicationService, mockVatRegistrationService, returnFrequencyView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val emptyReturns: VatApplication = VatApplication()
  val voluntary = true

  "returnsFrequencyPage" should {
    "return OK when returns are present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Monthly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are present with AAS" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Annual))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return Other when returns are present without AAS" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Quarterly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(false))

      callAuthorised(testController.show) { result =>
        status(result) mustBe SEE_OTHER
      }
    }
  }

  "submitReturnsFrequency" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.ReturnsFrequencyController.submit)

    "redirect to the Join Flat Rate page when they select the monthly option" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Monthly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ReturnsFrequencyForm.monthlyKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the TaxRepController when they select the monthly option and overseas entity does not have a fixed establishment in IsleOfManOrUK" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(partyType = Individual, fixedEstablishmentInManOrUk = false)))
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Monthly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ReturnsFrequencyForm.monthlyKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.TaxRepController.show.url)
      }
    }

    "redirect to the account periods page when they select the quarterly option" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Quarterly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ReturnsFrequencyForm.quarterlyKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/submit-vat-returns")
      }
    }

    "redirect to the last month of accounting year page when they select the annual option" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Annual))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ReturnsFrequencyForm.annualKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/last-month-of-accounting-year")
      }
    }

    "return BAD_REQUEST when no option is selected" in new Setup {
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(true))
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ""
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when an invalid option is submitted" in new Setup {
      when(movkVatApplicationService.isEligibleForAAS(any(), any(), any()))
        .thenReturn(Future.successful(false))
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }
}
