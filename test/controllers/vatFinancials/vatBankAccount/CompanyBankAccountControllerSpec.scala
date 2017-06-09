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

import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccount
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class CompanyBankAccountControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends CompanyBankAccountController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())

  s"GET ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()}" should {

    "return HTML when there's a Company Bank Account model in S4L" in {
      save4laterReturns2(CompanyBankAccount(CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES))()

      submitAuthorised(Controller.show(),
        fakeRequest.withFormUrlEncodedBody("companyBankAccountRadio" -> "")) {
        _ includesText "Do you have a bank account set up in the name of your company?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[CompanyBankAccount]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Do you have a bank account set up in the name of your company?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNothing2[CompanyBankAccount]()
    when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

    callAuthorised(Controller.show) {
      _ includesText "Do you have a bank account set up in the name of your company?"
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }
  }

  s"POST ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.submit()} with Company Bank Account selected Yes" should {

    "return 303" in {
      save4laterExpectsSave[CompanyBankAccount]()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES)) {
        _ redirectsTo s"$contextRoot/business-bank-account-details"
      }
    }
  }

  s"POST ${vatFinancials.vatBankAccount.routes.CompanyBankAccountController.submit()} with Company Bank Account selected No" should {

    "return 303" in {
      save4laterExpectsSave[CompanyBankAccount]()
      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("companyBankAccountRadio" -> CompanyBankAccount.COMPANY_BANK_ACCOUNT_NO)) {
        _ redirectsTo s"$contextRoot/estimate-vat-taxable-turnover-next-12-months"
      }
    }
  }
}