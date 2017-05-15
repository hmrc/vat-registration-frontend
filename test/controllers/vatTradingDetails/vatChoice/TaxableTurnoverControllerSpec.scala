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

package controllers.vatTradingDetails.vatChoice

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class TaxableTurnoverControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object TestTaxableTurnoverController extends TaxableTurnoverController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.TaxableTurnoverController.show())

  s"GET ${routes.TaxableTurnoverController.show()}" should {

    "return HTML when there's a start date in S4L" in {
      val taxableTurnover = TaxableTurnover(TaxableTurnover.TAXABLE_YES)

      when(mockS4LService.fetchAndGet[TaxableTurnover]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(taxableTurnover)))

      submitAuthorised(TestTaxableTurnoverController.show(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> ""
      )) {
        _ includesText "VAT taxable turnover to be more than £83,000"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[TaxableTurnover]()
        (Matchers.eq(S4LKey[TaxableTurnover]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestTaxableTurnoverController.show) {
        _ includesText "VAT taxable turnover to be more than £83,000"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[TaxableTurnover]()
        (Matchers.eq(S4LKey[TaxableTurnover]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestTaxableTurnoverController.show) {
        _ includesText "VAT taxable turnover to be more than £83,000"
      }
    }
  }


  s"POST ${routes.TaxableTurnoverController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestTaxableTurnoverController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected Yes" should {

    "return 303" in {
      val returnCacheMapTaxableTurnover = CacheMap("", Map("" -> Json.toJson(TaxableTurnover(TaxableTurnover.TAXABLE_YES))))
      val returnCacheMapVoluntaryRegistration = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistration(VoluntaryRegistration.REGISTER_NO))))
      val returnCacheMapStartDate = CacheMap("", Map("" -> Json.toJson(StartDateView(dateType = StartDateView.COMPANY_REGISTRATION_DATE))))

      when(mockS4LService.saveForm[TaxableTurnover](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapTaxableTurnover))

      when(mockS4LService.saveForm[VoluntaryRegistration](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapVoluntaryRegistration))

      when(mockS4LService.saveForm[StartDateView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapStartDate))

      submitAuthorised(TestTaxableTurnoverController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_YES
      ))(_ redirectsTo s"$contextRoot/start-date-confirmation")

    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(TaxableTurnover(TaxableTurnover.TAXABLE_NO))))

      when(mockS4LService.saveForm[TaxableTurnover](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      submitAuthorised(TestTaxableTurnoverController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_NO
      ))(_ redirectsTo s"$contextRoot/voluntary-registration")

    }
  }

}
