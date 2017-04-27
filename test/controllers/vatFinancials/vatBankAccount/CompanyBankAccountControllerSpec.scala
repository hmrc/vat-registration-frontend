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

package controllers.vatFinancials.vatBankAccount

import builders.AuthBuilder
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatFinancials.vatBankAccount.CompanyBankAccount
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

class CompanyBankAccountControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object CompanyBankAccountController extends CompanyBankAccountController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())

  s"GET ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()}" should {

    "return HTML when there's a Company Bank Account model in S4L" in {
      val companyBankAccount = CompanyBankAccount(CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES)

      when(mockS4LService.fetchAndGet[CompanyBankAccount]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(companyBankAccount)))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you have a bank account set up in the name of your company?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[CompanyBankAccount]()
        (Matchers.eq(S4LKey[CompanyBankAccount]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(CompanyBankAccountController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you have a bank account set up in the name of your company?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[CompanyBankAccount]()
      (Matchers.eq(S4LKey[CompanyBankAccount]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(CompanyBankAccountController.show) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Do you have a bank account set up in the name of your company?")
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.submit()} with Company Bank Account selected Yes" should {

    "return 303" in {
      val returnCacheMapCompanyBankAccount = CacheMap("", Map("" -> Json.toJson(CompanyBankAccount(CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES))))

      when(mockS4LService.saveForm[CompanyBankAccount]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCompanyBankAccount))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/bank-details"
      }

    }
  }

  s"POST ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.submit()} with Company Bank Account selected No" should {

    "return 303" in {
      val returnCacheMapCompanyBankAccount = CacheMap("", Map("" -> Json.toJson(CompanyBankAccount(CompanyBankAccount.COMPANY_BANK_ACCOUNT_NO))))

      when(mockS4LService.saveForm[CompanyBankAccount]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCompanyBankAccount))

      when(mockVatRegistrationService.deleteElement(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/estimate-vat-turnover"
      }

    }
  }
}