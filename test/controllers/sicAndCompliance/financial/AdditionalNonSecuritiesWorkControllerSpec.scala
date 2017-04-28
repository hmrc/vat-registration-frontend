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
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
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

class AdditionalNonSecuritiesWorkControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object AdditionalNonSecuritiesWorkController extends AdditionalNonSecuritiesWorkController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AdditionalNonSecuritiesWorkController.show())

  s"GET ${routes.AdditionalNonSecuritiesWorkController.show()}" should {

    "return HTML when there's a Additional Non Securities Work model in S4L" in {
      when(mockS4LService.fetchAndGet[AdditionalNonSecuritiesWork]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(AdditionalNonSecuritiesWork(true))))

      AuthBuilder.submitWithAuthorisedUser(AdditionalNonSecuritiesWorkController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesRadio" -> ""
      )) {
        _ includesText "Does the company do additional work (excluding securities) when introducing a client to a financial service provider?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[AdditionalNonSecuritiesWork]()
        (Matchers.eq(S4LKey[AdditionalNonSecuritiesWork]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
       _ includesText "Does the company do additional work (excluding securities) when introducing a client to a financial service provider?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[AdditionalNonSecuritiesWork]()
      (Matchers.eq(S4LKey[AdditionalNonSecuritiesWork]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(AdditionalNonSecuritiesWorkController.show) {
     _ includesText "Does the company do additional work (excluding securities) when introducing a client to a financial service provider?"
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(AdditionalNonSecuritiesWorkController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      ))(_ isA Status.BAD_REQUEST)

    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.submit()} with Additional Non Securities Work Yes selected" should {

    "return 303" in {
      val returnCacheMapAdditionalNonSecuritiesWork = CacheMap("", Map("" -> Json.toJson(AdditionalNonSecuritiesWork(true))))

      when(mockVatRegistrationService.deleteElements(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(true))

      when(mockS4LService.saveForm[AdditionalNonSecuritiesWork]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapAdditionalNonSecuritiesWork))

      AuthBuilder.submitWithAuthorisedUser(AdditionalNonSecuritiesWorkController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "true"
      )) { response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.submit()} with Additional Non Securities Work No selected" should {

    "return 303" in {
      val returnCacheMapAdditionalNonSecuritiesWork = CacheMap("", Map("" -> Json.toJson(AdditionalNonSecuritiesWork(false))))

      when(mockVatRegistrationService.deleteElements(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(mockS4LService.saveForm[AdditionalNonSecuritiesWork]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapAdditionalNonSecuritiesWork))

      AuthBuilder.submitWithAuthorisedUser(AdditionalNonSecuritiesWorkController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "false"
      )) { response =>
          response redirectsTo s"$contextRoot/provides-discretionary-investment-management-services"
      }

    }
  }
}