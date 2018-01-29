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

package features.sicAndCompliance.controllers

import connectors.KeystoreConnect
import features.sicAndCompliance.services.SicAndComplianceService
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import mocks.SicAndComplianceServiceMock
import features.sicAndCompliance.models.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class LabourComplianceControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
                                     with VatRegistrationFixture with MockMessages with SicAndComplianceServiceMock {

  trait Setup {
    val controller: LabourComplianceController = new LabourComplianceController {
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val sicAndCompService: SicAndComplianceService = mockSicAndComplianceService
    }

    mockAllMessages
    mockWithCurrentProfile(Some(currentProfile))
  }


  s"GET ${routes.LabourComplianceController.showProvideWorkers()}" should {
    "return HTML where getSicAndCompliance returns the view models wih labour already entered" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showProvideWorkers()) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(controller.showProvideWorkers) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.LabourComplianceController.submitProvideWorkers()}" should {
    val fakeRequest = FakeRequest(routes.LabourComplianceController.showProvideWorkers())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitProvideWorkers(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with company provide workers Yes selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour.copy(companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)))))

      submitAuthorised(controller.submitProvideWorkers(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_YES
      ))(_ redirectsTo routes.LabourComplianceController.showWorkers().url)
    }

    "return 303 with company provide workers No selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour.copy(companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)))))

      submitAuthorised(controller.submitProvideWorkers(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_NO
      ))(_ redirectsTo controllers.routes.TradingDetailsController.tradingNamePage().url)
    }
  }

  s"GET ${routes.LabourComplianceController.showWorkers()}" should {
    "return HTML when there's a Workers model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showWorkers) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(controller.showWorkers) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.LabourComplianceController.submitWorkers()}" should {
    val fakeRequest = FakeRequest(routes.LabourComplianceController.showWorkers())

    "return 400 with Empty data" in new Setup {

      submitAuthorised(controller.submitWorkers(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with less than 8 workers entered" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitWorkers(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      )) {
        result =>
          result redirectsTo controllers.routes.TradingDetailsController.tradingNamePage().url
      }
    }

    "return 303 with 8 or more workers entered" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitWorkers(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "8"
      )) {
        result =>
          result redirectsTo s"$contextRoot/provides-workers-on-temporary-contracts"
      }
    }
  }

  s"GET ${routes.LabourComplianceController.showTemporaryContracts()}" should {
    "return HTML when there's a Temporary Contracts model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showTemporaryContracts) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(controller.showTemporaryContracts) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.LabourComplianceController.submitTemporaryContracts()}" should {
    val fakeRequest = FakeRequest(routes.LabourComplianceController.showTemporaryContracts())

    "return 400 with Empty data" in new Setup {

      submitAuthorised(controller.submitTemporaryContracts(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with TemporaryContracts Yes selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitTemporaryContracts(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/provides-skilled-workers"
      }
    }

    "return 303 with TemporaryContracts No selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitTemporaryContracts(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingDetailsController.tradingNamePage().url
      }
    }
  }

  s"GET ${routes.LabourComplianceController.showSkilledWorkers()}" should {
    "return HTML when there's a Company Provide Skilled Workers model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showSkilledWorkers) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(controller.showSkilledWorkers) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.LabourComplianceController.submitSkilledWorkers()}" should {
    val fakeRequest = FakeRequest(routes.LabourComplianceController.showSkilledWorkers())

    "return 400 with Empty data" in new Setup {

      submitAuthorised(controller.submitSkilledWorkers(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with company provide Skilled workers Yes selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitSkilledWorkers(), fakeRequest.withFormUrlEncodedBody(
        "skilledWorkersRadio" -> SkilledWorkers.SKILLED_WORKERS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingDetailsController.tradingNamePage().url
      }
    }

    "return 303 with company provide Skilled workers No selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitSkilledWorkers(), fakeRequest.withFormUrlEncodedBody(
        "skilledWorkersRadio" -> SkilledWorkers.SKILLED_WORKERS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingDetailsController.tradingNamePage().url
      }
    }
  }
}
