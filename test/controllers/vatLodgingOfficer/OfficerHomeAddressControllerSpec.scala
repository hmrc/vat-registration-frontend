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
import models.api.{ScrsAddress, VatLodgingOfficer}
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class OfficerHomeAddressControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestOfficerHomeAddressController extends OfficerHomeAddressController(ds)(mockS4LService, mockVatRegistrationService, mockPPService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())

  val address = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  s"GET ${routes.OfficerHomeAddressController.show()}" should {

    when(mockPPService.getOfficerAddressList()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(Seq(address)))
    mockKeystoreCache[Seq[ScrsAddress]]("OfficerAddressList", CacheMap("", Map.empty))


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer(address)))

      when(mockS4LService.fetchAndGet[OfficerHomeAddressView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerHomeAddressController.show()) {
        _ includesText "What is your home address"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)

      when(mockS4LService.fetchAndGet[OfficerHomeAddressView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerHomeAddressController.show()) {
        _ includesText "What is your home address"
      }
    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestOfficerHomeAddressController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with selected address" should {

    "return 303" in {
      val savedAddressView = OfficerHomeAddressView(address.id, Some(address))
      val returnOfficerHomeAddressView = CacheMap("", Map("" -> Json.toJson(savedAddressView)))

      when(mockS4LService.saveForm[OfficerHomeAddressView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerHomeAddressView))
      when(mockPPService.getOfficerAddressList()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Seq(address)))
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", Some(Seq(address)))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerHomeAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> address.id)
      )(_ redirectsTo s"$contextRoot/business-activity-description")

    }
  }

  s"POST ${routes.OfficerHomeAddressController.submit()} with 'other address' selected" should {

    "return 303" in {
      val savedAddressView = OfficerHomeAddressView(address.id, Some(address))
      val returnOfficerHomeAddressView = CacheMap("", Map("" -> Json.toJson(savedAddressView)))

      when(mockS4LService.saveForm[OfficerHomeAddressView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerHomeAddressView))
      when(mockPPService.getOfficerAddressList()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Seq(address)))
      mockKeystoreFetchAndGet[Seq[ScrsAddress]]("OfficerAddressList", Some(Seq(address)))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerHomeAddressController.submit(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> "other")
      )(_ redirectsTo s"$contextRoot/business-activity-description")

    }
  }



}
