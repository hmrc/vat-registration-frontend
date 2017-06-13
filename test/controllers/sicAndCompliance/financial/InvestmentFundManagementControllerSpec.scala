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
import models.view.sicAndCompliance.financial.InvestmentFundManagement
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class InvestmentFundManagementControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object InvestmentFundManagementController extends InvestmentFundManagementController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }
  val fakeRequest = FakeRequest(routes.InvestmentFundManagementController.show())

  s"GET ${routes.InvestmentFundManagementController.show()}" should {

    "return HTML when there's a Investment Fund Management model in S4L" in {
      save4laterReturnsViewModel(InvestmentFundManagement(true))()
      submitAuthorised(InvestmentFundManagementController.show(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> ""
      )) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[InvestmentFundManagement]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(InvestmentFundManagementController.show) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[InvestmentFundManagement]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(InvestmentFundManagementController.show) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }
  }

  s"POST ${routes.InvestmentFundManagementController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(InvestmentFundManagementController.submit(),
        fakeRequest.withFormUrlEncodedBody("bogus" -> "nonsense")) { result =>
        result isA 400
      }
    }

    "return 303 with Investment Fund Management Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsNothing2[BusinessActivityDescription]()
      save4laterExpectsSave[InvestmentFundManagement]()

      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/manages-funds-not-included-in-this-list")
    }

    "return 303 with Investment Fund Management No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[InvestmentFundManagement]()

      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/business-bank-account")
    }
  }
}