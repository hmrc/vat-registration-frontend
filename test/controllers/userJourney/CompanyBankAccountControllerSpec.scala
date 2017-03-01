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
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.{CompanyBankAccount, EstimateZeroRatedSales, ZeroRatedSales}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CompanyBankAccountControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object CompanyBankAccountController extends CompanyBankAccountController(mockS4LService, mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.CompanyBankAccountController.show())

  s"GET ${routes.CompanyBankAccountController.show()}" should {

    "return HTML when there's a Company Bank Account model in S4L" in {
      val companyBankAccount = CompanyBankAccount("")

      when(mockS4LService.fetchAndGet[CompanyBankAccount](Matchers.eq(CacheKeys.CompanyBankAccount.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(companyBankAccount)))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> ""
      )){

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you have a bank account set up in the name of your company?")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[ZeroRatedSales](Matchers.eq(CacheKeys.CompanyBankAccount.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[ZeroRatedSales]]()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(CompanyBankAccountController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you have a bank account set up in the name of your company?")
      }
    }
  }


  s"POST ${routes.ZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe  Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.CompanyBankAccountController.submit()} with Company Bank Account selected Yes" should {

    "return 303" in {
      val returnCacheMapCompanyBankAccount = CacheMap("", Map("" -> Json.toJson(CompanyBankAccount())))

      when(mockS4LService.saveForm[CompanyBankAccount]
        (Matchers.eq(CacheKeys.CompanyBankAccount.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[CompanyBankAccount]]()))
        .thenReturn(Future.successful(returnCacheMapCompanyBankAccount))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/estimate-vat-turnover"
      }

    }
  }

  s"POST ${routes.CompanyBankAccountController.submit()} with Company Bank Account selected No" should {

    "return 303" in {
      val returnCacheMapCompanyBankAccount = CacheMap("", Map("" -> Json.toJson(CompanyBankAccount())))

      when(mockS4LService.saveForm[CompanyBankAccount]
        (Matchers.eq(CacheKeys.CompanyBankAccount.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[CompanyBankAccount]]()))
        .thenReturn(Future.successful(returnCacheMapCompanyBankAccount))

      AuthBuilder.submitWithAuthorisedUser(CompanyBankAccountController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/estimate-vat-turnover"
      }

    }
  }

}
