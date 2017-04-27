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

package controllers.vatTradingDetails.vatEuTrading

import builders.AuthBuilder
import controllers.vatTradingDetails
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatEuTrading.EuGoods
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

class EuGoodsControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object EuGoodsController extends EuGoodsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())

  s"GET ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()}" should {

    "return HTML when there's a Eu Goods model in S4L" in {
      val euGoods = EuGoods(EuGoods.EU_GOODS_YES)

      when(mockS4LService.fetchAndGet[EuGoods]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(euGoods)))

      AuthBuilder.submitWithAuthorisedUser(EuGoodsController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you import or export goods from or to countries outside the EU?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[EuGoods]()
        (Matchers.eq(S4LKey[EuGoods]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(EuGoodsController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you import or export goods from or to countries outside the EU?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[EuGoods]()
      (Matchers.eq(S4LKey[EuGoods]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(EuGoodsController.show) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Do you import or export goods from or to countries outside the EU?")
    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(EuGoodsController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.submit()} with Eu Goods Yes selected" should {

    "return 303" in {
      val returnCacheMapEuGoods = CacheMap("", Map("" -> Json.toJson(EuGoods(EuGoods.EU_GOODS_YES))))

      when(mockS4LService.saveForm[EuGoods]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapEuGoods))

      AuthBuilder.submitWithAuthorisedUser(EuGoodsController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_YES
      )) {
        response =>
          response redirectsTo s"$contextRoot/apply-eori"
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.submit()} with Eu Goods No selected" should {

    "return 303" in {
      val returnCacheMapEuGoods = CacheMap("", Map("" -> Json.toJson(EuGoods(EuGoods.EU_GOODS_NO))))

      when(mockS4LService.saveForm[EuGoods]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapEuGoods))

      AuthBuilder.submitWithAuthorisedUser(EuGoodsController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_NO
      )) {
        response =>
          response redirectsTo s"$contextRoot/business-activity-description"
      }

    }
  }
}