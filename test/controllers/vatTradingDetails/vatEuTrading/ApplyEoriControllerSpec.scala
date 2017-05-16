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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatTradingDetails.vatEuTrading.ApplyEori
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class ApplyEoriControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  import cats.instances.future._
  import cats.syntax.applicative._

  object ApplyEoriController extends ApplyEoriController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ApplyEoriController.show())

  s"GET ${routes.ApplyEoriController.show()}" should {

    "return HTML when there's a Apply Eori model in S4L" in {
      save4laterReturns(ApplyEori(ApplyEori.APPLY_EORI_YES))
      submitAuthorised(ApplyEoriController.show(), fakeRequest.withFormUrlEncodedBody("applyEoriRadio" -> "")) {
        _ includesText "You need to apply for an Economic Operator Registration and Identification (EORI) number"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing[ApplyEori]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(ApplyEoriController.show) {
        _ includesText "You need to apply for an Economic Operator Registration and Identification (EORI) number"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNothing[ApplyEori]()
    when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

    callAuthorised(ApplyEoriController.show) {
      _ includesText "You need to apply for an Economic Operator Registration and Identification (EORI) number"
    }
  }

  s"POST ${routes.ApplyEoriController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }

  }

  s"POST ${routes.ApplyEoriController.submit()} with Apply Eori Yes selected" should {

    "return 303" in {
      val returnCacheMapApplyEori = CacheMap("", Map("" -> Json.toJson(ApplyEori(ApplyEori.APPLY_EORI_YES))))
      when(mockS4LService.saveForm[ApplyEori](any())(any(), any(), any())).thenReturn(returnCacheMapApplyEori.pure)

      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_YES)
      )) {
        _ redirectsTo s"$contextRoot/your-home-address"
      }

    }
  }

  s"POST ${routes.ApplyEoriController.submit()} with Apply Eori No selected" should {

    "return 303" in {
      val returnCacheMapApplyEori = CacheMap("", Map("" -> Json.toJson(ApplyEori(ApplyEori.APPLY_EORI_NO))))
      when(mockS4LService.saveForm[ApplyEori](any())(any(), any(), any())).thenReturn(returnCacheMapApplyEori.pure)

      submitAuthorised(ApplyEoriController.submit(), fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> String.valueOf(ApplyEori.APPLY_EORI_NO)
      )) {
        _ redirectsTo s"$contextRoot/your-home-address"
      }
    }

  }
}