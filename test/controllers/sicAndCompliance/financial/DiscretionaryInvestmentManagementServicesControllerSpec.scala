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
import models.view.sicAndCompliance.financial.DiscretionaryInvestmentManagementServices
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class DiscretionaryInvestmentManagementServicesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object DiscretionaryInvestmentManagementServicesController extends
    DiscretionaryInvestmentManagementServicesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.DiscretionaryInvestmentManagementServicesController.show())

  s"GET ${routes.DiscretionaryInvestmentManagementServicesController.show()}" should {

    "return HTML when there's a DiscretionaryInvestmentManagementServices model in S4L" in {
      save4laterReturnsViewModel(DiscretionaryInvestmentManagementServices(true))()

      submitAuthorised(DiscretionaryInvestmentManagementServicesController.show(), fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> ""
      )) {
        _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[DiscretionaryInvestmentManagementServices]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(DiscretionaryInvestmentManagementServicesController.show) {
        _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[DiscretionaryInvestmentManagementServices]()
    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(DiscretionaryInvestmentManagementServicesController.show) {
        _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
      }
    }
  }

  s"POST ${routes.DiscretionaryInvestmentManagementServicesController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(DiscretionaryInvestmentManagementServicesController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with Provide Discretionary Investment Management Services Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[DiscretionaryInvestmentManagementServices]()

      submitAuthorised(DiscretionaryInvestmentManagementServicesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }

    "return 303 with Provide Discretionary Investment Management Services No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsNoViewModel[BusinessActivityDescription]()
      save4laterExpectsSave[DiscretionaryInvestmentManagementServices]()

      submitAuthorised(DiscretionaryInvestmentManagementServicesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/involved-in-leasing-vehicles-or-equipment")

    }
  }
}