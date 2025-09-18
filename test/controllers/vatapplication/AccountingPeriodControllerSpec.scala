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

import _root_.models._
import fixtures.VatRegistrationFixture
import forms.vatapplication.AccountingPeriodForm
import models.api.NETP
import models.api.vatapplication._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.vatapplication.AccountingPeriodView

import scala.concurrent.Future

class AccountingPeriodControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {

  class Setup {
    val accountingPeriodView: AccountingPeriodView = app.injector.instanceOf[AccountingPeriodView]
    val testController = new AccountingPeriodController(
      mockSessionService, mockAuthClientConnector, movkVatApplicationService, mockVatRegistrationService, accountingPeriodView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val emptyReturns: VatApplication = VatApplication()
  val voluntary = true

  "accountsPeriodPage" should {
    "return OK when returns are present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(JanuaryStagger))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitAccountsPeriod" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.AccountingPeriodController.submit)

    "redirect to the TaskListController when they select the jan apr jul oct option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(JanuaryStagger))))
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.janStaggerKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the TaskListController when they select the feb may aug nov option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(FebruaryStagger))))
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.febStaggerKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the TaskListController when they select the mar may sep dec option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(MarchStagger))))
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.marStaggerKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the TaxRepController if overseas entity does not have a fixed establishment in IsleOfManOrUK" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(MarchStagger))))
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(partyType = NETP, fixedEstablishmentInManOrUk = false)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.marStaggerKey
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.TaxRepController.show.url)
      }
    }

    "return 400 when they do not select an option" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> ""
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when they submit an invalid choice" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
