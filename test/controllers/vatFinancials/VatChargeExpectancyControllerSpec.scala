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

package controllers.vatFinancials

import builders.AuthBuilder
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatFinancials.VatChargeExpectancy
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class VatChargeExpectancyControllerSpec extends VatRegSpec with VatRegistrationFixture {


  object TestVatChargeExpectancyController extends VatChargeExpectancyController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  import cats.instances.future._
  import cats.syntax.applicative._

  val fakeRequest = FakeRequest(vatFinancials.routes.VatChargeExpectancyController.show())

  s"GET ${vatFinancials.routes.VatChargeExpectancyController.show()}" should {

    "return HTML when there's a Vat Charge Expectancy model in S4L" in {
      val vatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)

      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()(any(), any(), any()))
        .thenReturn(Some(vatChargeExpectancy).pure)

      submitAuthorised(TestVatChargeExpectancyController.show(), fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> ""
      )) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()
        (Matchers.eq(S4LKey[VatChargeExpectancy]), any(), any())).thenReturn(None.pure)

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestVatChargeExpectancyController.show) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()
        (Matchers.eq(S4LKey[VatChargeExpectancy]), any(), any())).thenReturn(None.pure)

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(TestVatChargeExpectancyController.show) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }

  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestVatChargeExpectancyController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected Yes" should {

    "return 303" in {
      val returnCacheMapVatChargeExpectancy = CacheMap("", Map("" -> Json.toJson(VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES))))
      when(mockS4LService.saveForm[VatChargeExpectancy](any())(any(), any(), any())).thenReturn(returnCacheMapVatChargeExpectancy.pure)

      submitAuthorised(TestVatChargeExpectancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_YES
      )) {
        _ redirectsTo s"$contextRoot/vat-return-frequency"
      }
    }

  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO))))
      val returnCacheMapReturnFrequency = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency(VatReturnFrequency.MONTHLY))))

      when(mockS4LService.saveForm[VatChargeExpectancy](any())(any(), any(), any())).thenReturn(returnCacheMap.pure)
      when(mockS4LService.saveForm[VatReturnFrequency](any())(any(), any(), any())).thenReturn(returnCacheMapReturnFrequency.pure)

      submitAuthorised(TestVatChargeExpectancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_NO
      )) {
        _ redirectsTo s"$contextRoot/accounting-period"
      }
    }

  }

}
