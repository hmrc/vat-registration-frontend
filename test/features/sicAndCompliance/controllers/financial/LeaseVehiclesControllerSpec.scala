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

package controllers.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatSicAndCompliance
import models.view.sicAndCompliance.financial.LeaseVehicles
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class LeaseVehiclesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object LeaseVehiclesController extends LeaseVehiclesController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(routes.LeaseVehiclesController.show())

  s"GET ${routes.LeaseVehiclesController.show()}" should {
    "return HTML when there's a Lease Vehicles or Equipment - model in S4L" in {
      save4laterReturnsViewModel(LeaseVehicles(true))()
      mockGetCurrentProfile()
      callAuthorised(LeaseVehiclesController.show()) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[LeaseVehicles]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(LeaseVehiclesController.show) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[LeaseVehicles]()
    when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]())).thenReturn(emptyVatScheme.pure)
    mockGetCurrentProfile()
      callAuthorised(LeaseVehiclesController.show) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }
  }

  s"POST ${routes.LeaseVehiclesController.show()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()
      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }

    "redirects to next screen in the flow -  with Lease Vehicles or Equipment - Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      save4laterExpectsSave[LeaseVehicles]()
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      mockGetCurrentProfile()
      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "leaseVehiclesRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/trading-name")
    }

    "redirects to next screen in the flow -  with Lease Vehicles or Equipment - No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      save4laterExpectsSave[LeaseVehicles]()
      mockGetCurrentProfile()
      submitAuthorised(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody("leaseVehiclesRadio" -> "false")) {
        _ redirectsTo s"$contextRoot/provides-investment-fund-management-services"
      }
    }
  }
}
