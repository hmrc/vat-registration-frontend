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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalNonSecuritiesWorkControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object AdditionalNonSecuritiesWorkController
    extends AdditionalNonSecuritiesWorkController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AdditionalNonSecuritiesWorkController.show())

  s"GET ${routes.AdditionalNonSecuritiesWorkController.show()}" should {

    "return HTML when there's a Additional Non Securities Work model in S4L" in {
      save4laterReturns(AdditionalNonSecuritiesWork(true))

      submitAuthorised(AdditionalNonSecuritiesWorkController.show(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesRadio" -> "")) {
        _ includesText "Does the company do additional work (excluding securities) " +
          "when introducing a client to a financial service provider?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing[AdditionalNonSecuritiesWork]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
        _ includesText "Does the company do additional work (excluding securities)" +
          " when introducing a client to a financial service provider?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNothing[AdditionalNonSecuritiesWork]()
    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(AdditionalNonSecuritiesWorkController.show) {
      _ includesText "Does the company do additional work (excluding securities) " +
        "when introducing a client to a financial service provider?"
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)

    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.submit()} with Additional Non Securities Work Yes selected" should {

    "return 303" in {
      val returnCacheMapAdditionalNonSecuritiesWork = CacheMap("", Map("" -> Json.toJson(AdditionalNonSecuritiesWork(true))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockS4LService.saveForm[AdditionalNonSecuritiesWork](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapAdditionalNonSecuritiesWork))

      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/company-bank-account")
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.submit()} with Additional Non Securities Work No selected" should {

    "return 303" in {
      val returnCacheMapAdditionalNonSecuritiesWork = CacheMap("", Map("" -> Json.toJson(AdditionalNonSecuritiesWork(false))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockS4LService.saveForm[AdditionalNonSecuritiesWork](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapAdditionalNonSecuritiesWork))

      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/provides-discretionary-investment-management-services")

    }
  }
}