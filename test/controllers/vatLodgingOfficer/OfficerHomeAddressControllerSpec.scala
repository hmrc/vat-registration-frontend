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

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.{DateOfBirth, ScrsAddress, VatLodgingOfficer}
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.FakeRequest

class OfficerHomeAddressControllerSpec extends VatRegSpec
  with VatRegistrationFixture with S4LMockSugar with BeforeAndAfterEach {

  object Controller extends OfficerHomeAddressController(ds)(
    mockS4LService,
    mockVatRegistrationService,
    mockPPService,
    mockAddressLookupConnector) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  s"GET ${routes.OfficerHomeAddressController.show()}" should {

    when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
    mockKeystoreCache[Seq[ScrsAddress]]("OfficerAddressList", dummyCacheMap)

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName, formerName, validOfficerContactDetails)))
      save4laterReturnsNothing2[OfficerHomeAddressView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "What is your home address"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)
      save4laterReturnsNothing2[OfficerHomeAddressView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "What is your home address"
      }
    }

  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with Empty data" should {

    "return 400" in {
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with selected address" should {

    "return 303" in {
      save4laterExpectsSave[OfficerHomeAddressView]()
      when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
      when(mockVatRegistrationService.submitVatLodgingOfficer()(any())).thenReturn(validLodgingOfficer.pure)
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", Some(Seq(address)))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/business-contact")

      verify(mockVatRegistrationService).submitVatLodgingOfficer()(any())
    }

  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with selected address but no address list in keystore" should {

    "return 303" in {
      save4laterExpectsSave[OfficerHomeAddressView]()
      when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
      when(mockVatRegistrationService.submitVatLodgingOfficer()(any())).thenReturn(validLodgingOfficer.pure)
      mockKeystoreFetchAndGet("OfficerAddressList", Option.empty[Seq[ScrsAddress]])

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/business-contact")

      verify(mockVatRegistrationService).submitVatLodgingOfficer()(any())
    }

  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with 'other address' selected" should {

    "redirect the user to TxM address capture page" in {
      when(mockAddressLookupConnector.getOnRampUrl(any[Call])(any(), any())).thenReturn(Call("GET", "TxM").pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> "other")
      )(_ redirectsTo "TxM")
    }

  }


  s"GET ${routes.OfficerHomeAddressController.acceptFromTxm()}" should {

    "save an address and redirect to next page" in {
      save4laterExpectsSave[OfficerHomeAddressView]()
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      callAuthorised(Controller.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/business-contact"
      }

      val expectedAddressView = OfficerHomeAddressView(address.id, Some(address))
      verify(mockS4LService).updateViewModel(Matchers.eq(expectedAddressView))(any(), any(), any(), any())
    }

  }

}
