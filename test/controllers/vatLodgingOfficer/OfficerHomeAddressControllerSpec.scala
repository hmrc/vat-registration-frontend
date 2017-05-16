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

import builders.AuthBuilder
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{DateOfBirth, ScrsAddress, VatLodgingOfficer}
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.{Matchers, Mockito}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class OfficerHomeAddressControllerSpec extends VatRegSpec with VatRegistrationFixture {

  import cats.instances.future._
  import cats.syntax.applicative._

  implicit val headerCarrier = HeaderCarrier()

  object TestOfficerHomeAddressController extends OfficerHomeAddressController(ds)(
    mockS4LService,
    mockVatRegistrationService,
    mockPPService,
    mockAddressLookupConnector) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))
  val dummyCacheMap = CacheMap("", Map.empty)

  s"GET ${routes.OfficerHomeAddressController.show()}" should {

    when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
    mockKeystoreCache[Seq[ScrsAddress]]("OfficerAddressList", dummyCacheMap)


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer(address, DateOfBirth.empty, "")))

      when(mockS4LService.fetchAndGet[OfficerHomeAddressView]()(any(), any(), any())).thenReturn(None.pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(TestOfficerHomeAddressController.show()) {
        _ includesText "What is your home address"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)

      when(mockS4LService.fetchAndGet[OfficerHomeAddressView]()(any(), any(), any())).thenReturn(None.pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(TestOfficerHomeAddressController.show()) {
        _ includesText "What is your home address"
      }
    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with Empty data" should {

    "return 400" in {
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", None)

      submitAuthorised(TestOfficerHomeAddressController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with selected address but no address list in keystore" should {

    "return 303" in {
      val savedAddressView = OfficerHomeAddressView(address.id, Some(address))
      val returnOfficerHomeAddressView = CacheMap("", Map("" -> Json.toJson(savedAddressView)))

      when(mockS4LService.saveForm[OfficerHomeAddressView](any())(any(), any(), any()))
        .thenReturn(returnOfficerHomeAddressView.pure)
      when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
      mockKeystoreFetchAndGet("OfficerAddressList", Option.empty[Seq[ScrsAddress]])

      submitAuthorised(
        TestOfficerHomeAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/business-activity-description")

    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with selected address" should {

    "return 303" in {
      val savedAddressView = OfficerHomeAddressView(address.id, Some(address))
      val returnOfficerHomeAddressView = CacheMap("", Map("" -> Json.toJson(savedAddressView)))

      when(mockS4LService.saveForm[OfficerHomeAddressView](any())(any(), any(), any())).thenReturn(returnOfficerHomeAddressView.pure)
      when(mockPPService.getOfficerAddressList()(any())).thenReturn(Seq(address).pure)
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", Some(Seq(address)))

      submitAuthorised(
        TestOfficerHomeAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/business-activity-description")

    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with 'other address' selected" should {

    "redirect the user to TxM address capture page" in {
      val savedAddressView = OfficerHomeAddressView(address.id, Some(address))
      val returnOfficerHomeAddressView = CacheMap("", Map("" -> Json.toJson(savedAddressView)))

      when(mockAddressLookupConnector.getOnRampUrl(any[Call])(any(), any())).thenReturn(Call("GET", "TxM").pure)

      submitAuthorised(
        TestOfficerHomeAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> "other")
      )(_ redirectsTo "TxM")
    }
  }


  s"GET ${routes.OfficerHomeAddressController.acceptFromTxm()}" should {
    "save an address and redirect to next page" in {
      Mockito.reset(mockS4LService)
      when(mockAddressLookupConnector.getAddress(any())(any())).thenReturn(address.pure)
      when(mockS4LService.saveForm(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)

      callAuthorised(TestOfficerHomeAddressController.acceptFromTxm("addressId")) {
        _ redirectsTo s"$contextRoot/business-activity-description"
      }

      val expectedAddressView = OfficerHomeAddressView(address.id, Some(address))
      verify(mockS4LService, times(1)).saveForm(Matchers.eq(expectedAddressView))(any(), any(), any())
    }
  }


}
