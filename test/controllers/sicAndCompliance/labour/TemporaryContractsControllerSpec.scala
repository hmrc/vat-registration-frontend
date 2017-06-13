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

package controllers.sicAndCompliance.labour

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.labour.TemporaryContracts
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class TemporaryContractsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TemporaryContractsController extends TemporaryContractsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.TemporaryContractsController.show())

  s"GET ${sicAndCompliance.labour.routes.TemporaryContractsController.show()}" should {

    "return HTML when there's a Temporary Contracts model in S4L" in {
      save4laterReturnsViewModel(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO))()

      submitAuthorised(TemporaryContractsController.show(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers on temporary contracts?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[TemporaryContracts]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(TemporaryContractsController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers on temporary contracts?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[TemporaryContracts]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TemporaryContractsController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers on temporary contracts?")
      }
    }
  }

  s"POST ${sicAndCompliance.labour.routes.TemporaryContractsController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with TemporaryContracts Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsNothing2[BusinessActivityDescription]()
      save4laterExpectsSave[TemporaryContracts]()

      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/provides-skilled-workers"
      }
    }

    "return 303 with TemporaryContracts No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[TemporaryContracts]()

      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/business-bank-account"
      }
    }
  }
}