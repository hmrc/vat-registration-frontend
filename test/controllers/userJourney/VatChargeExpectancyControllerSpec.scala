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

package controllers.userJourney

import builders.AuthBuilder
import controllers.userJourney.vatFinancials.VatChargeExpectancyController
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CacheKey
import models.view.vatFinancials.{VatChargeExpectancy, VatReturnFrequency}
import models.view.{VatChargeExpectancy, VatReturnFrequency}
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

class VatChargeExpectancyControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVatChargeExpectancyController extends VatChargeExpectancyController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VatChargeExpectancyController.show())

  s"GET ${routes.VatChargeExpectancyController.show()}" should {

    "return HTML when there's a Vat Charge Expectancy model in S4L" in {
      val vatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)

      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(vatChargeExpectancy)))

      AuthBuilder.submitWithAuthorisedUser(TestVatChargeExpectancyController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you expect to reclaim more VAT than you charge?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()
        (Matchers.eq(CacheKey[VatChargeExpectancy]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVatChargeExpectancyController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you expect to reclaim more VAT than you charge?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()
        (Matchers.eq(CacheKey[VatChargeExpectancy]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestVatChargeExpectancyController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you expect to reclaim more VAT than you charge?")
      }
    }
  }


  s"POST ${routes.VatChargeExpectancyController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVatChargeExpectancyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected Yes" should {

    "return 303" in {
      val returnCacheMapVatChargeExpectancy = CacheMap("", Map("" -> Json.toJson(VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES))))

      when(mockS4LService.saveForm[VatChargeExpectancy]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapVatChargeExpectancy))

      AuthBuilder.submitWithAuthorisedUser(TestVatChargeExpectancyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/vat-return-frequency"
      }

    }
  }

  s"POST ${routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO))))
      val returnCacheMapReturnFrequency = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency(VatReturnFrequency.MONTHLY))))

      when(mockS4LService.saveForm[VatChargeExpectancy](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      when(mockS4LService.saveForm[VatReturnFrequency](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapReturnFrequency))

      AuthBuilder.submitWithAuthorisedUser(TestVatChargeExpectancyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/accounting-period"
      }

    }
  }

}
