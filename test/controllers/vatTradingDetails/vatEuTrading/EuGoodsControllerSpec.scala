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

import controllers.vatTradingDetails
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatTradingDetails.vatEuTrading.EuGoods
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class EuGoodsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {



  object EuGoodsController extends EuGoodsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())

  s"GET ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()}" should {

    "return HTML when there's a Eu Goods model in S4L" in {
      val euGoods = EuGoods(EuGoods.EU_GOODS_YES)

      save4laterReturnsViewModel(euGoods)()

      submitAuthorised(EuGoodsController.show(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Will the company trade VAT taxable goods or services with countries outside the EU?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[EuGoods]()

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(EuGoodsController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Will the company trade VAT taxable goods or services with countries outside the EU?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[EuGoods]()

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(EuGoodsController.show) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Will the company trade VAT taxable goods or services with countries outside the EU?")
    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.submit()} with Eu Goods Yes selected" should {

    "return 303" in {
      save4laterExpectsSave[EuGoods]()

      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_YES
      )) {
        response =>
          response redirectsTo s"$contextRoot/apply-economic-operator-registration-identification-number"
      }

    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.submit()} with Eu Goods No selected" should {

    "return 303" in {
      save4laterExpectsSave[EuGoods]()
      when(mockVatRegistrationService.submitTradingDetails()(any())).thenReturn(validVatTradingDetails.pure)

      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_NO
      )) {
        response =>
          response redirectsTo s"$contextRoot/your-home-address"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any())
    }
  }
}