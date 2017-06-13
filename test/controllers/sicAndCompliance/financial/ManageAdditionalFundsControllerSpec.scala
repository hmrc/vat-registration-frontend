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

package controllers.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.financial.ManageAdditionalFunds
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ManageAdditionalFundsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object ManageAdditionalFundsController extends ManageAdditionalFundsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  val fakeRequest = FakeRequest(routes.ManageAdditionalFundsController.show())

  s"GET ${routes.ManageAdditionalFundsController.show()}" should {

    "return HTML when there's a Manage Additional Funds model in S4L" in {
      save4laterReturnsViewModel(ManageAdditionalFunds(true))()

      submitAuthorised(ManageAdditionalFundsController.show(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> ""
      )) {
        _ includesText "Does the company manage any funds that are not included in this list?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[ManageAdditionalFunds]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(ManageAdditionalFundsController.show) {
       _ includesText "Does the company manage any funds that are not included in this list?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[ManageAdditionalFunds]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(ManageAdditionalFundsController.show) {
        _ includesText "Does the company manage any funds that are not included in this list?"
      }
    }
  }

  s"POST ${routes.ManageAdditionalFundsController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with Manage Additional Funds Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[ManageAdditionalFunds]()

      submitAuthorised(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> "true"
      )) {
        response => response redirectsTo s"$contextRoot/business-bank-account"
      }
    }

    "return 303 with Manage Additional Funds No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[ManageAdditionalFunds]()

      submitAuthorised(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> "false"
      )) {
        response => response redirectsTo s"$contextRoot/business-bank-account"
      }
    }
  }
}