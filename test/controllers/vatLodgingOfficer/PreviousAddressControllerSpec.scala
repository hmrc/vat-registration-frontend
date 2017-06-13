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
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.mvc.Call
import play.api.test.FakeRequest

class PreviousAddressControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestPreviousAddressController extends PreviousAddressController(ds)(mockS4LService, mockVatRegistrationService, mockAddressLookupConnector) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatLodgingOfficer.routes.PreviousAddressController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.show()}" should {

    reset(mockVatRegistrationService)

    "return HTML when there's a previous address question in S4L" in {
      save4laterReturnsViewModel(PreviousAddressView(yesNo = true))()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[PreviousAddressView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[PreviousAddressView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(TestPreviousAddressController.show) {
        _ includesText "Have you lived at your current address for 3 years or more?"
      }
    }
  }

  s"POST ${vatLodgingOfficer.routes.PreviousAddressController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestPreviousAddressController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => result isA 400
      }

    }
  }

  s"POST ${vatLodgingOfficer.routes.PreviousAddressController.submit()} with previous address question no selected" should {

    "redirect the user to TxM address capture page" in {
      when(mockAddressLookupConnector.getOnRampUrl(any[Call])(any(), any())).thenReturn(Call("GET", "TxM").pure)

      submitAuthorised(TestPreviousAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      )(_ redirectsTo "TxM")
    }

  }

  s"POST ${vatLodgingOfficer.routes.PreviousAddressController.submit()} with previous address question yes selected" should {

    "return 303" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockVatRegistrationService.submitVatLodgingOfficer()(any())).thenReturn(validLodgingOfficer.pure)
      submitAuthorised(TestPreviousAddressController.submit(), fakeRequest.withFormUrlEncodedBody(
        "previousAddressQuestionRadio" -> "true"
      )) {
        _ redirectsTo s"$contextRoot/company-contact-details"
      }

      verify(mockVatRegistrationService).submitVatLodgingOfficer()(any())
    }
  }

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.acceptFromTxm()}" should {

    "save an address and redirect to next page" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      callAuthorised(TestPreviousAddressController.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/company-contact-details"
      }

      val expectedAddressView = PreviousAddressView(false, Some(address))
      verify(mockS4LService).updateViewModel(Matchers.eq(expectedAddressView))(any(), any(), any(), any())
    }

  }

  s"GET ${vatLodgingOfficer.routes.PreviousAddressController.changeAddress()}" should {

    "save an address and redirect to next page" in {
      save4laterExpectsSave[PreviousAddressView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      callAuthorised(TestPreviousAddressController.changeAddress()) {
        (_ redirectsTo "TxM")
      }

    }
  }


}
