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

import connectors.KeystoreConnector
import controllers.vatFinancials
import controllers.vatFinancials.EstimateVatTurnoverKey
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatFinancials
import models.view.vatFinancials.EstimateVatTurnover
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class CompanyBankAccountDetailsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends CompanyBankAccountDetailsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  val validBankAccountFormData = Seq(
    "accountName" -> "Some account name",
    "accountNumber" -> "12345678",
    "sortCode.part1" -> "11",
    "sortCode.part2" -> "22",
    "sortCode.part3" -> "33"
  )

  val validCompanyBankAccountDetails = CompanyBankAccountDetails("name", "12345678", "11-11-11")

  s"GET ${vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show()}" should {

    "return HTML when there's a CompanyBankAccountDetails model in S4L" in {
      save4laterReturnsViewModel(validCompanyBankAccountDetails)()

      callAuthorised(Controller.show()) {
        _ includesText "What are your business bank account details?"
      }
    }

    "return HTML when there's invalid sort code stored in S4L" in {
      save4laterReturnsViewModel(validCompanyBankAccountDetails.copy(sortCode = "foo--bar"))()

      callAuthorised(Controller.show()) {
        _ includesText "What are your business bank account details?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "What are your business bank account details?"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "What are your business bank account details?"
      }
    }
  }


  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()}" should {
    "return 400 with Empty data" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }

    "redirect to start of FRS flow with valid Company Bank Account Details and turnover estimate less than threshold" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      save4laterReturnsViewModel(EstimateVatTurnover(149000L))()
      when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
      }
    }

    "redirect to summary with valid Company Bank Account Details and turnover estimate greater than threshold" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      save4laterReturnsViewModel(EstimateVatTurnover(151000))()
      when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }
    }

    "redirect to start of FRS flow with valid Company Bank Account Details and no turnover estimate found" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
      }
    }

    "return 400 with invalid Company Bank Account Details" in {
      save4laterExpectsSave[CompanyBankAccountDetails]()
      val invalidFormData = validBankAccountFormData.drop(1)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(invalidFormData: _*))(_ isA 400)
    }

  }

}
