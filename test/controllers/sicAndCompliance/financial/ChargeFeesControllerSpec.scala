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

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.financial.ChargeFees
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ChargeFeesControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object ChargeFeesController extends ChargeFeesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ChargeFeesController.show())

  s"GET ${routes.ChargeFeesController.show()}" should {

    "return HTML when there's a Charge Fees model in S4L" in {
      val chargeFees = ChargeFees(true)

      when(mockS4LService.fetchAndGet[ChargeFees]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(chargeFees)))

      AuthBuilder.submitWithAuthorisedUser(ChargeFeesController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company charge fees for introducing clients to financial service providers?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[ChargeFees]()
        (Matchers.eq(S4LKey[ChargeFees]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(ChargeFeesController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company charge fees for introducing clients to financial service providers?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[ChargeFees]()
      (Matchers.eq(S4LKey[ChargeFees]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(ChargeFeesController.show, mockAuthConnector) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Does the company charge fees for introducing clients to financial service providers?")
    }
  }

  s"POST ${routes.ChargeFeesController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(ChargeFeesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.ChargeFeesController.submit()} with Charge Fees Yes selected" should {

    "return 303" in {
      val returnCacheMapChargeFees = CacheMap("", Map("" -> Json.toJson(ChargeFees(true))))

      when(mockS4LService.saveForm[ChargeFees]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapChargeFees))

      AuthBuilder.submitWithAuthorisedUser(ChargeFeesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> "true"
      )) {
        response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }

  s"POST ${routes.ChargeFeesController.submit()} with Charge Fees No selected" should {

    "return 303" in {
      val returnCacheMapChargeFees = CacheMap("", Map("" -> Json.toJson(ChargeFees(false))))

      when(mockS4LService.saveForm[ChargeFees]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapChargeFees))

      AuthBuilder.submitWithAuthorisedUser(ChargeFeesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> "false"
      )) {
        response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }
}