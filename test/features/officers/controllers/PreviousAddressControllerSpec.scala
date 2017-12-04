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

package controllers.vatLodgingOfficer

import controllers.vatLodgingOfficer
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.ScrsAddress
import models.view.vatLodgingOfficer.PreviousAddressView
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Call
import play.api.test.FakeRequest

class PreviousAddressControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestPreviousAddressController extends PreviousAddressController(
    ds,
    mockAddressService,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(vatLodgingOfficer.routes.PreviousAddressController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.show()}" should {

    "return HTML when there's a previous address question in S4L" in {
      save4laterReturnsViewModel(PreviousAddressView(yesNo = true))()
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[PreviousAddressView]()
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[PreviousAddressView]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }
  }

  s"POST ${vatLodgingOfficer.routes.PreviousAddressController.submit()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()
      submitAuthorised(TestPreviousAddressController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => result isA 400
      }
    }

    "return 303 redirect the user to TxM address capture page" in {
      when(mockAddressService.getJourneyUrl(any(), any())(any(), any())).thenReturn(Call("GET", "TxM").pure)
      mockGetCurrentProfile()
      submitAuthorised(TestPreviousAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      )(_ redirectsTo "TxM")
    }

    "return 303 with previous address question yes selected" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(emptyVatScheme.pure)
      when(mockVatRegistrationService.submitVatLodgingOfficer(any(),any())).thenReturn(validLodgingOfficer.pure)
      mockGetCurrentProfile()
      submitAuthorised(TestPreviousAddressController.submit(), fakeRequest.withFormUrlEncodedBody(
        "previousAddressQuestionRadio" -> "true"
      )) {
        _ redirectsTo s"$contextRoot/where-will-company-carry-out-most-of-its-business-activities"
      }

      verify(mockVatRegistrationService).submitVatLodgingOfficer(any(), any())
    }
  }

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.acceptFromTxm()}" should {
    "save an address and redirect to next page" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockAddressService.getAddressById(any())(any())).thenReturn(address.pure)
      when(mockVatRegistrationService.submitVatLodgingOfficer(any(), any())).thenReturn(validLodgingOfficer.pure)
      mockGetCurrentProfile()
      callAuthorised(TestPreviousAddressController.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/where-will-company-carry-out-most-of-its-business-activities"
      }

      verify(mockS4LService).updateViewModel(any(), any())(any(), any(), any(), any(), any())
    }

  }

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.changeAddress()}" should {

    "save an address and redirect to next page" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      when(mockAddressService.getJourneyUrl(any(), any())(any(), any())).thenReturn(Call("GET", "TxM").pure)
      mockGetCurrentProfile()
      callAuthorised(TestPreviousAddressController.changeAddress()) {
        _ redirectsTo "TxM"
      }
    }
  }
}
