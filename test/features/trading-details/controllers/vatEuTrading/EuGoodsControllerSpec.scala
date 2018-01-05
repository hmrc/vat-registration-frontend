/*
 * Copyright 2018 HM Revenue & Customs
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
import models.CurrentProfile
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class EuGoodsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object EuGoodsController extends EuGoodsController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())

  s"GET ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()}" should {
    "return HTML when there's a Eu Goods model in S4L" in {
      val euGoods = EuGoods(EuGoods.EU_GOODS_YES)

      save4laterReturnsViewModel(euGoods)()

      mockGetCurrentProfile()

      submitAuthorised(EuGoodsController.show(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> ""
      )) {
        _ includesText "Will the company trade VAT taxable goods or services with countries outside the EU?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[EuGoods]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(ArgumentMatchers.any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(EuGoodsController.show) {
        _ includesText "Will the company trade VAT taxable goods or services with countries outside the EU?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[EuGoods]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(ArgumentMatchers.any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(EuGoodsController.show) {
        _ includesText "Will the company trade VAT taxable goods or services with countries outside the EU?"
      }
    }
  }

  s"POST ${vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()}" should {
    "return 400 with Empty data" in {

      mockGetCurrentProfile()

      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with Eu Goods Yes selected" in {
      save4laterExpectsSave[EuGoods]()

      mockGetCurrentProfile()

      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_YES
      )) {
       _ redirectsTo s"$contextRoot/apply-economic-operator-registration-identification-number"
      }

    }

    "return 303 with Eu Goods No selected" in {
      save4laterExpectsSave[EuGoods]()
      save4laterExpectsSave[ApplyEori]()

      mockGetCurrentProfile()

      submitAuthorised(EuGoodsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> EuGoods.EU_GOODS_NO
      )) {
       _ redirectsTo s"$contextRoot/estimate-vat-taxable-turnover-next-12-months"
      }
    }
  }
}
