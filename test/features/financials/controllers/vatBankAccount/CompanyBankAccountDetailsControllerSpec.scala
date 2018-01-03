/*
 * Copyright 2018 HM Revenue & Customs
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
import controllers.vatFinancials.EstimateVatTurnoverKey
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatFinancials
import models.view.vatFinancials.EstimateVatTurnover
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.Future

class CompanyBankAccountDetailsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends CompanyBankAccountDetailsController(
    mockBankAccountReputationService,
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

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

      mockGetCurrentProfile()

      callAuthorised(Controller.show()) {
        _ includesText "What are your business bank account details?"
      }
    }

    "return HTML when there's invalid sort code stored in S4L" in {
      save4laterReturnsViewModel(validCompanyBankAccountDetails.copy(sortCode = "foo--bar"))()

      mockGetCurrentProfile()

      callAuthorised(Controller.show()) {
        _ includesText "What are your business bank account details?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(validVatScheme.pure)

      mockGetCurrentProfile()

      callAuthorised(Controller.show) {
        _ includesText "What are your business bank account details?"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)

      mockGetCurrentProfile()

      callAuthorised(Controller.show) {
        _ includesText "What are your business bank account details?"
      }
    }
  }


  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }

    "redirect to start of FRS flow with valid Company Bank Account Details and turnover estimate less than threshold" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      save4laterReturnsViewModel(EstimateVatTurnover(149000L))()
      when(mockVatRegistrationService.submitVatFinancials()(any(), any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())
      when(mockBankAccountReputationService.bankDetailsModulusCheck(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(true))
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
      }
    }

    "redirect to summary with valid Company Bank Account Details and turnover estimate greater than threshold" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      save4laterReturnsViewModel(EstimateVatTurnover(151000))()
      when(mockVatRegistrationService.submitVatFinancials()(any(), any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())
      when(mockBankAccountReputationService.bankDetailsModulusCheck(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(true))
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }
    }

    "redirect to start of FRS flow with valid Company Bank Account Details and no turnover estimate found" in {
      mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))
      save4laterExpectsSave[CompanyBankAccountDetails]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      when(mockVatRegistrationService.submitVatFinancials()(any(), any())).thenReturn(validVatFinancials.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatFinancials())
      when(mockBankAccountReputationService.bankDetailsModulusCheck(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(true))
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
      }
    }

    "return 400 with invalid Company Bank Account Details" in {
      save4laterExpectsSave[CompanyBankAccountDetails]()
      val invalidFormData = validBankAccountFormData.drop(1)
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(invalidFormData: _*))(_ isA 400)
    }

    "return 400 with invalid Company Bank Account Details entered" in {
     mockGetCurrentProfile()
      when(mockBankAccountReputationService.bankDetailsModulusCheck(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(false))
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ isA 400
      }
    }

    "an upstream5xxexception should be encountered if a 500 is recieved from BARS" in {
      mockGetCurrentProfile()
      when(mockBankAccountReputationService.bankDetailsModulusCheck(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse("",500,500)))
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(validBankAccountFormData: _*)) {
        _ failedWith classOf[Upstream5xxResponse]
      }
    }
  }
}
