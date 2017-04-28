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

package controllers.vatFinancials.vatAccountingPeriod

import builders.AuthBuilder
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import forms.vatFinancials.vatAccountingPeriod.VatReturnFrequencyForm
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class VatReturnFrequencyControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVatReturnFrequencyController extends VatReturnFrequencyController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())

  s"GET ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show()}" should {

    "return HTML when there's a Vat Return Frequency model in S4L" in {
      val vatReturnFrequency = VatReturnFrequency(VatReturnFrequency.MONTHLY)

      when(mockS4LService.fetchAndGet[VatReturnFrequency]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(vatReturnFrequency)))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.show(), fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> ""
      )) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[VatReturnFrequency]()
        (Matchers.eq(S4LKey[VatReturnFrequency]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVatReturnFrequencyController.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[VatReturnFrequency]()
        (Matchers.eq(S4LKey[VatReturnFrequency]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestVatReturnFrequencyController.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }
  }


  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Monthly" should {

    "return 303" in {
      val returnCacheMapVatReturnFrequency = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency(VatReturnFrequency.MONTHLY))))
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod(""))))

      when(mockS4LService.saveForm[VatReturnFrequency](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapVatReturnFrequency))

      when(mockS4LService.saveForm[AccountingPeriod](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      when(mockVatRegistrationService.deleteElement(any())(any()))
        .thenReturn(Future.successful(()))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY
      ))(_ redirectsTo s"$contextRoot/summary")

    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Quarterly" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency(VatReturnFrequency.QUARTERLY))))
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod(AccountingPeriod.FEB_MAY_AUG_NOV))))

      when(mockS4LService.saveForm[VatReturnFrequency](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      when(mockS4LService.saveForm[AccountingPeriod](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.QUARTERLY
      ))(_ redirectsTo s"$contextRoot/accounting-period")

    }
  }

}
