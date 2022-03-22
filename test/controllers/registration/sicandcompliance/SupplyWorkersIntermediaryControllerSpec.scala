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

package controllers.registration.sicandcompliance

import fixtures.VatRegistrationFixture
import models.api.{Individual, UkCompany}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, SicAndComplianceServiceMock}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.labour.intermediary_supply

import scala.concurrent.Future

class SupplyWorkersIntermediaryControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with SicAndComplianceServiceMock with MockApplicantDetailsService {

  trait Setup {
    val view = app.injector.instanceOf[intermediary_supply]
    val controller: SupplyWorkersIntermediaryController = new SupplyWorkersIntermediaryController(
      mockAuthClientConnector,
      mockSessionService,
      mockSicAndComplianceService,
      mockApplicantDetailsService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show}" should {
    "return OK when there's a Temporary Contracts model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }

    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.submit}" should {
    val fakeRequest = FakeRequest(controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show)

    "return BAD_REQUEST with Empty data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to PartyType resolver with Yes selected for UkCompany" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )) {
        response =>
          status(response) mustBe SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingNameResolverController.resolve.url
      }
    }

    "redirect to PartyType resolver with No selected for UkCompany" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )) {
        response =>
          status(response) mustBe SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingNameResolverController.resolve.url
      }
    }

    "redirect to PartyType resolver with Yes selected for Sole Trader" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )) {
        response =>
          status(response) mustBe SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingNameResolverController.resolve.url
      }
    }

    "redirect to PartyType resolver with No selected for Sole Trader" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )) {
        response =>
          status(response) mustBe SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingNameResolverController.resolve.url
      }
    }
  }
}
