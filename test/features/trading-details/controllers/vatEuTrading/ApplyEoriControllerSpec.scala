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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.view.vatTradingDetails.vatEuTrading.ApplyEori
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class ApplyEoriControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object ApplyEoriController extends ApplyEoriController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(routes.ApplyEoriController.show())

  s"GET ${routes.ApplyEoriController.show()}" should {
    "return HTML when there's a Apply Eori model in S4L" in {
      save4laterReturnsViewModel(ApplyEori(ApplyEori.APPLY_EORI_YES))()

      mockGetCurrentProfile()

      submitAuthorised(ApplyEoriController.show(), fakeRequest.withFormUrlEncodedBody("applyEoriRadio" -> "")) {
        _ includesText "You need an Economic Operator Registration and Identification (EORI) number"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[ApplyEori]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(validVatScheme.pure)

      callAuthorised(ApplyEoriController.show) {
        _ includesText "You need an Economic Operator Registration and Identification (EORI) number"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[ApplyEori]()
    when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)

    mockGetCurrentProfile()

    callAuthorised(ApplyEoriController.show) {
      _ includesText "You need an Economic Operator Registration and Identification (EORI) number"
    }
  }

  s"POST ${routes.ApplyEoriController.show()} with Empty data" should {
    "return 400" in {
      mockGetCurrentProfile()

      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }
  }

  s"POST ${routes.ApplyEoriController.submit()} with Apply Eori Yes selected" should {
    "return 303" in {
      save4laterExpectsSave[ApplyEori]()

      mockGetCurrentProfile()

      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_YES)
      )) {
        _ redirectsTo s"$contextRoot/estimate-vat-taxable-turnover-next-12-months"
      }
    }
  }

  s"POST ${routes.ApplyEoriController.submit()} with Apply Eori No selected" should {
    "return 303" in {
      save4laterExpectsSave[ApplyEori]()

      mockGetCurrentProfile()

      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_NO)
      )) {
        _ redirectsTo s"$contextRoot/estimate-vat-taxable-turnover-next-12-months"
      }
    }
  }
}
