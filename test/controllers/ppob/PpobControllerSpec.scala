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

package controllers.ppob

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.ScrsAddress
import models.view.ppob.PpobView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.mvc.Call
import play.api.test.FakeRequest

class PpobControllerSpec extends VatRegSpec
  with VatRegistrationFixture with S4LMockSugar {

  object Controller extends PpobController(ds)(
    mockS4LService,
    mockVatRegistrationService,
    mockPPService,
    mockAddressLookupConnector) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.ppob.routes.PpobController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  s"GET ${routes.PpobController.show()}" should {

    when(mockPPService.getPpobAddressList()(any())).thenReturn(Seq(address).pure)
    mockKeystoreCache[Seq[ScrsAddress]]("PpobAddressList", dummyCacheMap)

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(ppob = Some(address))
      save4laterReturnsNothing2[PpobView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "Where will the company carry out most of its business activities"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(ppob = None)
      save4laterReturnsNothing2[PpobView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "Where will the company carry out most of its business activities"
      }
    }

  }

  s"POST ${routes.PpobController.submit()} with Empty data" should {

    "return 400" in {
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("PpobAddressList", None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

  }

  s"POST ${routes.PpobController.submit()} with selected address" should {

    "return 303" in {
      save4laterExpectsSave[PpobView]()
      when(mockPPService.getPpobAddressList()(any())).thenReturn(Seq(address).pure)
      when(mockVatRegistrationService.submitPpob()(any())).thenReturn(scrsAddress.pure)
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("PpobAddressList", Some(Seq(address)))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/describe-what-company-does")
      //verify(mockVatRegistrationService).submitPpob()(any())

    }

  }

  s"POST ${routes.PpobController.submit()} with selected address but no address list in keystore" should {

    "return 303" in {
      save4laterExpectsSave[PpobView]()
      when(mockPPService.getPpobAddressList()(any())).thenReturn(Seq(address).pure)
      when(mockVatRegistrationService.submitPpob()(any())).thenReturn(scrsAddress.pure)
      mockKeystoreFetchAndGet("PpobAddressList", Option.empty[Seq[ScrsAddress]])

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/describe-what-company-does")
      //verify(mockVatRegistrationService).submitPpob()(any())

    }

  }

  s"POST ${routes.PpobController.submit()} with 'other address' selected" should {

    "redirect the user to TxM address capture page" in {
      when(mockAddressLookupConnector.getOnRampUrl(any[Call])(any(), any())).thenReturn(Call("GET", "TxM").pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "other")
      )(_ redirectsTo "TxM")
    }

  }


  s"GET ${routes.PpobController.acceptFromTxm()}" should {

    "save an address and redirect to next page" in {
      save4laterExpectsSave[PpobView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      callAuthorised(Controller.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/describe-what-company-does"
      }

      val expectedAddressView = PpobView(address.id, Some(address))
      verify(mockS4LService).updateViewModel(Matchers.eq(expectedAddressView))(any(), any(), any(), any())
    }

  }

}
