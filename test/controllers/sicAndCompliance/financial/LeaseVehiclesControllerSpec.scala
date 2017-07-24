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
import models.view.sicAndCompliance.financial.LeaseVehicles
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class LeaseVehiclesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object LeaseVehiclesController extends LeaseVehiclesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.LeaseVehiclesController.show())

  s"GET ${routes.LeaseVehiclesController.show()}" should {

    "return HTML when there's a Lease Vehicles or Equipment - model in S4L" in {
      save4laterReturnsViewModel(LeaseVehicles(true))()

      callAuthorised(LeaseVehiclesController.show()) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[LeaseVehicles]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(LeaseVehiclesController.show) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[LeaseVehicles]()
    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(emptyVatScheme.pure)

      callAuthorised(LeaseVehiclesController.show) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }
  }

  s"POST ${routes.LeaseVehiclesController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }

    "redirects to next screen in the flow -  with Lease Vehicles or Equipment - Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()
      save4laterExpectsSave[LeaseVehicles]()

      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "leaseVehiclesRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }

    "redirects to next screen in the flow -  with Lease Vehicles or Equipment - No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterReturnsNoViewModel[BusinessActivityDescription]()
      save4laterExpectsSave[LeaseVehicles]()

      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody("leaseVehiclesRadio" -> "false")) {
        _ redirectsTo s"$contextRoot/provides-investment-fund-management-services"
      }
    }
  }
}