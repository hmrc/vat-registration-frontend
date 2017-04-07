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
import controllers.userJourney.vatTradingDetails.vatEuTrading.ApplyEoriController
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatEuTrading.ApplyEori
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

class ApplyEoriControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object ApplyEoriController extends ApplyEoriController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show())

  s"GET ${vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show()}" should {

    "return HTML when there's a Apply Eori model in S4L" in {
      val euGoods = ApplyEori(ApplyEori.APPLY_EORI_YES)

      when(mockS4LService.fetchAndGet[ApplyEori]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(euGoods)))

      AuthBuilder.submitWithAuthorisedUser(ApplyEoriController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("You need to apply for an Economic Operator Registration and Identification (EORI) number")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[ApplyEori]()
        (Matchers.eq(S4LKey[ApplyEori]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(ApplyEoriController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("You need to apply for an Economic Operator Registration and Identification (EORI) number")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[ApplyEori]()
      (Matchers.eq(S4LKey[ApplyEori]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(ApplyEoriController.show, mockAuthConnector) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("You need to apply for an Economic Operator Registration and Identification (EORI) number")
    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(ApplyEoriController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.ApplyEoriController.submit()} with Apply Eori Yes selected" should {

    "return 303" in {
      val returnCacheMapApplyEori = CacheMap("", Map("" -> Json.toJson(ApplyEori(ApplyEori.APPLY_EORI_YES))))

      when(mockS4LService.saveForm[ApplyEori]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapApplyEori))

      AuthBuilder.submitWithAuthorisedUser(ApplyEoriController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_YES)
      )) {
        response =>
          response redirectsTo s"$contextRoot/business-activity-description"
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.ApplyEoriController.submit()} with Apply Eori No selected" should {

    "return 303" in {
      val returnCacheMapApplyEori = CacheMap("", Map("" -> Json.toJson(ApplyEori(ApplyEori.APPLY_EORI_NO))))

      when(mockS4LService.saveForm[ApplyEori]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapApplyEori))

      AuthBuilder.submitWithAuthorisedUser(ApplyEoriController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_NO)
      )) {
        response =>
          response redirectsTo s"$contextRoot/business-activity-description"
      }

    }
  }
}