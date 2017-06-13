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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class TaxableTurnoverControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestTaxableTurnoverController extends TaxableTurnoverController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.TaxableTurnoverController.show())

  s"GET ${routes.TaxableTurnoverController.show()}" should {

    "return HTML when there's a start date in S4L" in {
      val taxableTurnover = TaxableTurnover(TaxableTurnover.TAXABLE_YES)

      save4laterReturnsViewModel(taxableTurnover)()

      submitAuthorised(TestTaxableTurnoverController.show(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> ""
      )) {
        _ includesText "VAT taxable sales of more than £85,000 in the 30 days"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[TaxableTurnover]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestTaxableTurnoverController.show) {
        _ includesText "VAT taxable sales of more than £85,000 in the 30 days"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[TaxableTurnover]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestTaxableTurnoverController.show) {
        _ includesText "VAT taxable sales of more than £85,000 in the 30 days"
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
      save4laterExpectsSave[TaxableTurnover]()
      save4laterExpectsSave[VoluntaryRegistration]()
      save4laterExpectsSave[StartDateView]()

      submitAuthorised(TestTaxableTurnoverController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_YES
      ))(_ redirectsTo s"$contextRoot/who-is-registering-the-company-for-vat")

    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected No" should {

    "return 303" in {
      save4laterExpectsSave[TaxableTurnover]()

      submitAuthorised(TestTaxableTurnoverController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_NO
      ))(_ redirectsTo s"$contextRoot/do-you-want-to-register-voluntarily")

    }
  }

}
