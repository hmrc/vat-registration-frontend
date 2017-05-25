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

package controllers.sicAndCompliance.financial

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.financial.InvestmentFundManagement
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class InvestmentFundManagementControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object InvestmentFundManagementController extends InvestmentFundManagementController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.InvestmentFundManagementController.show())

  s"GET ${routes.InvestmentFundManagementController.show()}" should {

    "return HTML when there's a Investment Fund Management model in S4L" in {
      val chargeFees = InvestmentFundManagement(true)

      when(mockS4LService.fetchAndGet[InvestmentFundManagement]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(chargeFees)))

      submitAuthorised(InvestmentFundManagementController.show(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> ""
      )) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[InvestmentFundManagement]()
        (Matchers.eq(S4LKey[InvestmentFundManagement]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(InvestmentFundManagementController.show) {
        _ includesText "Does the company provide investment fund management services?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[InvestmentFundManagement]()
      (Matchers.eq(S4LKey[InvestmentFundManagement]), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(InvestmentFundManagementController.show) {
      _ includesText "Does the company provide investment fund management services?"
    }
  }

  s"POST ${routes.InvestmentFundManagementController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(InvestmentFundManagementController.submit(),
        fakeRequest.withFormUrlEncodedBody("bogus" -> "nonsense")) { result =>
        result isA 400
      }
    }
  }

  s"POST ${routes.InvestmentFundManagementController.submit()} with Investment Fund Management Yes selected" should {

    "return 303" in {
      val returnCacheMapInvestmentFundManagement = CacheMap("", Map("" -> Json.toJson(InvestmentFundManagement(true))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.save[InvestmentFundManagement](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapInvestmentFundManagement))

      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/manages-funds-not-included-in-this-list")

    }
  }

  s"POST ${routes.InvestmentFundManagementController.submit()} with Investment Fund Management No selected" should {

    "return 303" in {
      val returnCacheMapInvestmentFundManagement = CacheMap("", Map("" -> Json.toJson(InvestmentFundManagement(false))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.save[InvestmentFundManagement](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapInvestmentFundManagement))

      submitAuthorised(InvestmentFundManagementController.submit(), fakeRequest.withFormUrlEncodedBody(
        "investmentFundManagementRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/company-bank-account")
    }
  }
}