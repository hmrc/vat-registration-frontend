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
import controllers.userJourney.sicAndCompliance.CulturalComplianceQ1Controller
import controllers.userJourney.vatFinancials.CompanyBankAccountController
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CacheKey
import models.view.sicAndCompliance.CulturalComplianceQ1
import models.view.vatFinancials.CompanyBankAccount
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CulturalComplianceQ1ControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object CulturalComplianceQ1Controller extends CulturalComplianceQ1Controller(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.routes.CulturalComplianceQ1Controller.show())

  s"GET ${sicAndCompliance.routes.CulturalComplianceQ1Controller.show()}" should {

    "return HTML when there's a Cultural Compliance Q1 model in S4L" in {
      val culturalComplianceQ1 = CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_NO)

      when(mockS4LService.fetchAndGet[CulturalComplianceQ1]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(culturalComplianceQ1)))

      AuthBuilder.submitWithAuthorisedUser(CulturalComplianceQ1Controller.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[CulturalComplianceQ1]()
        (Matchers.eq(CacheKey[CulturalComplianceQ1]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(CulturalComplianceQ1Controller.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[CulturalComplianceQ1]()
      (Matchers.eq(CacheKey[CulturalComplianceQ1]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(CulturalComplianceQ1Controller.show, mockAuthConnector) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
    }
  }

  s"POST ${sicAndCompliance.routes.CulturalComplianceQ1Controller.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(CulturalComplianceQ1Controller.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${sicAndCompliance.routes.CulturalComplianceQ1Controller.submit()} with not for profit Yes selected" should {

    "return 303" in {
      val returnCacheMapCulturalComplianceQ1 = CacheMap("", Map("" -> Json.toJson(CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_YES))))

      when(mockS4LService.saveForm[CulturalComplianceQ1]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCulturalComplianceQ1))

      AuthBuilder.submitWithAuthorisedUser(CulturalComplianceQ1Controller.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> CulturalComplianceQ1.NOT_PROFIT_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }

  s"POST ${sicAndCompliance.routes.CulturalComplianceQ1Controller.submit()} with not for profit No selected" should {

    "return 303" in {
      val returnCacheMapCulturalComplianceQ1 = CacheMap("", Map("" -> Json.toJson(CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_NO))))

      when(mockS4LService.saveForm[CulturalComplianceQ1]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCulturalComplianceQ1))

      AuthBuilder.submitWithAuthorisedUser(CulturalComplianceQ1Controller.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> CulturalComplianceQ1.NOT_PROFIT_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }
}