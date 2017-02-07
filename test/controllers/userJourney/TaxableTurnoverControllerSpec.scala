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
import enums.CacheKeys
import helpers.VatRegSpec
import models.view.TaxableTurnover
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TaxableTurnoverControllerSpec extends VatRegSpec {

  object TestTaxableTurnoverController extends TaxableTurnoverController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.TaxableTurnoverController.show())

  s"GET ${routes.TaxableTurnoverController.show()}" should {

    "return HTML Taxable Turnover page with no Selection" in {
      val taxableTurnover = TaxableTurnover("")

      when(mockS4LService.fetchAndGet[TaxableTurnover](Matchers.eq(CacheKeys.TaxableTurnover.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(taxableTurnover)))

      AuthBuilder.submitWithAuthorisedUser(TestTaxableTurnoverController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> ""
      )){

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT taxable turnover to be more than £83,000")
      }
    }
  }


  s"POST ${routes.TaxableTurnoverController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestTaxableTurnoverController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe  Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected Yes" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(TaxableTurnover.empty)))

      when(mockS4LService.saveForm[TaxableTurnover](Matchers.eq(CacheKeys.TaxableTurnover.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestTaxableTurnoverController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/trading-name"
      }

    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(TaxableTurnover.empty)))

      when(mockS4LService.saveForm[TaxableTurnover](Matchers.eq(CacheKeys.TaxableTurnover.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestTaxableTurnoverController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/voluntary-registration"
      }

    }
  }

}
