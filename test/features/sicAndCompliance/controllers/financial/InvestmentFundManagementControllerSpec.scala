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
import models.{CurrentProfile, S4LVatSicAndCompliance}
import models.view.sicAndCompliance.financial.InvestmentFundManagement
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class InvestmentFundManagementControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object InvestmentFundManagementController extends InvestmentFundManagementController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.InvestmentFundManagementController.show())

  s"GET ${routes.InvestmentFundManagementController.show()}" should {
    "return HTML when there's a Investment Fund Management model in S4L" in {
      save4laterReturnsViewModel(InvestmentFundManagement(true))()
      mockGetCurrentProfile()
      submitAuthorised(InvestmentFundManagementController.show(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> ""
      )) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[InvestmentFundManagement]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()
      callAuthorised(InvestmentFundManagementController.show) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[InvestmentFundManagement]()
    when(mockVatRegistrationService.getVatScheme()(any(), any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
    mockGetCurrentProfile()
      callAuthorised(InvestmentFundManagementController.show) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }
  }

  s"POST ${routes.InvestmentFundManagementController.show()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()
      submitAuthorised(InvestmentFundManagementController.submit(),
        fakeRequest.withFormUrlEncodedBody("bogus" -> "nonsense")) { result =>
        result isA 400
      }
    }

    "return 303 with Investment Fund Management Yes selected" in {
      save4laterExpectsSave[InvestmentFundManagement]()
      mockGetCurrentProfile()
      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/manages-funds-not-included-in-this-list")
    }

    "return 303 with Investment Fund Management No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterExpectsSave[InvestmentFundManagement]()
      save4laterReturns(S4LVatSicAndCompliance())
      mockGetCurrentProfile()
      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }
  }
}
