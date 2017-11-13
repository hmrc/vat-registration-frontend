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

package controllers.vatContact.ppob

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.api.ScrsAddress
import models.view.vatContact.ppob.PpobView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import play.api.mvc.Call
import play.api.test.FakeRequest

import scala.concurrent.Future

class PpobControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends PpobController(ds)(
    mockS4LService,
    mockVatRegistrationService,
    mockPPService,
    mockAddressLookupConnector) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatContact.ppob.routes.PpobController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  s"GET ${routes.PpobController.show()}" should {

    "return HTML when there's nothing in S4L" in {
      mockGetCurrentProfile()

      mockKeystoreCache[Seq[ScrsAddress]]("PpobAddressList", dummyCacheMap)
      save4laterReturnsNoViewModel[PpobView]()
      when(mockPPService.getPpobAddressList()(any(), any())).thenReturn(Seq(address).pure)

      callAuthorised(Controller.show()) {
        _ includesText "Where will the company carry out most of its business activities"
      }
    }

    "return HTML when view is present in S4L" in {
      mockGetCurrentProfile()

      save4laterReturnsViewModel(PpobView(addressId = address.id, address = Some(address)))()
      mockKeystoreCache[Seq[ScrsAddress]]("PpobAddressList", dummyCacheMap)
      when(mockPPService.getPpobAddressList()(any(), any())).thenReturn(Seq(address).pure)

      callAuthorised(Controller.show()) {
        _ includesText "Where will the company carry out most of its business activities"
      }
    }

  }

  s"POST ${routes.PpobController.submit()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("PpobAddressList", None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with selected address" in {
      mockGetCurrentProfile()

      save4laterExpectsSave[PpobView]()
      when(mockPPService.getPpobAddressList()(any(), any())).thenReturn(Seq(address).pure)
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("PpobAddressList", Some(Seq(address)))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/company-contact-details")
    }

    "return 303 with selected address but no address list in keystore" in {
      mockGetCurrentProfile()

      save4laterExpectsSave[PpobView]()
      when(mockPPService.getPpobAddressList()(any(), any())).thenReturn(Seq(address).pure)
      mockKeystoreFetchAndGet("PpobAddressList", Option.empty[Seq[ScrsAddress]])

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/company-contact-details")
    }

    "redirect the user to TxM address capture page with 'other address' selected" in {
      mockGetCurrentProfile()

      when(mockAddressLookupConnector.getOnRampUrl(any[Call])(any(), any())).thenReturn(Call("GET", "TxM").pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "other")
      )(_ redirectsTo "TxM")
    }

  }

  s"GET ${routes.PpobController.acceptFromTxm()}" should {
    "save an address and redirect to next page" in {
      mockGetCurrentProfile()

      save4laterExpectsSave[PpobView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      callAuthorised(Controller.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/company-contact-details"
      }

      verify(mockS4LService).updateViewModel(any(), any())(any(), any(), any(), any(), any())
    }
  }
}
