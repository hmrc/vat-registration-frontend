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
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CacheKey
import models.view.AccountingPeriod
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

class AccountingPeriodControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestAccountingPeriodController extends AccountingPeriodController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AccountingPeriodController.show())

  s"GET ${routes.AccountingPeriodController.show()}" should {

    "return HTML when there's a Accounting Period model in S4L" in {
      val accountingPeriod = AccountingPeriod()

      when(mockS4LService.fetchAndGet[AccountingPeriod]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(accountingPeriod)))

      AuthBuilder.submitWithAuthorisedUser(TestAccountingPeriodController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("When do you want your VAT Return periods to end?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contain data" in {
      when(mockS4LService.fetchAndGet[AccountingPeriod]()(Matchers.eq(CacheKey[AccountingPeriod]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestAccountingPeriodController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("When do you want your VAT Return periods to end?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contain no data" in {
      when(mockS4LService.fetchAndGet[AccountingPeriod]()
        (Matchers.eq(CacheKey[AccountingPeriod]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestAccountingPeriodController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("When do you want your VAT Return periods to end?")
      }
    }
  }

  s"POST ${routes.AccountingPeriodController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(
        TestAccountingPeriodController.submit(), mockAuthConnector,
        fakeRequest.withFormUrlEncodedBody(
        ))(status(_) mustBe Status.BAD_REQUEST)
    }
  }

  s"POST ${routes.AccountingPeriodController.submit()} with accounting period selected is January, April, July and October" should {

    "return 303" in {
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod())))

      when(mockS4LService.saveForm[AccountingPeriod]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestAccountingPeriodController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> AccountingPeriod.JAN_APR_JUL_OCT
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe "/vat-registration/summary"
      }

    }
  }

  s"POST ${routes.AccountingPeriodController.submit()} with accounting period selected is February, May, August and November" should {

    "return 303" in {
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod())))

      when(mockS4LService.saveForm[AccountingPeriod]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestAccountingPeriodController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> AccountingPeriod.FEB_MAY_AUG_NOV
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe "/vat-registration/summary"
      }

    }
  }

  s"POST ${routes.AccountingPeriodController.submit()} with accounting period selected is March, June, September and December" should {

    "return 303" in {
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod())))

      when(mockS4LService.saveForm[AccountingPeriod]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestAccountingPeriodController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> AccountingPeriod.MAR_JUN_SEP_DEC
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe "/vat-registration/summary"
      }

    }
  }

}
