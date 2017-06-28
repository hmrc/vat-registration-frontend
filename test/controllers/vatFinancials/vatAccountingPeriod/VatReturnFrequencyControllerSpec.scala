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

package controllers.vatFinancials.vatAccountingPeriod

import connectors.KeystoreConnector
import controllers.vatFinancials
import controllers.vatFinancials.EstimateVatTurnoverKey
import fixtures.VatRegistrationFixture
import forms.vatFinancials.vatAccountingPeriod.VatReturnFrequencyForm
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatFinancials.EstimateVatTurnover
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class VatReturnFrequencyControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends VatReturnFrequencyController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())

  s"GET ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show()}" should {

    "return HTML when there's a Vat Return Frequency model in S4L" in {
      save4laterReturnsViewModel(VatReturnFrequency(VatReturnFrequency.MONTHLY))()

      submitAuthorised(Controller.show(),
        fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> "")) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[VatReturnFrequency]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[VatReturnFrequency]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }
  }


  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Monthly" should {

    "redirect to summary page" when {

      "taxable turnover above the FRS threshold of 150k" in {
        save4laterReturnsViewModel(EstimateVatTurnover(200000L))() //above the 150k threshold
        save4laterExpectsSave[VatReturnFrequency]()
        save4laterExpectsSave[AccountingPeriod]()

        when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)
        when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
        when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)
        mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))

        submitAuthorised(Controller.submit(),
          fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
          _ redirectsTo s"$contextRoot/check-your-answers"
        }

        verify(mockVatRegistrationService).submitVatFinancials()(any())
      }

    }

    "redirect to start of FRS Flow" when {

      "estimated vat turnover less than 150k" in {
        save4laterReturnsViewModel(EstimateVatTurnover(100000L))() //below the 150k threshold
        save4laterExpectsSave[VatReturnFrequency]()
        save4laterExpectsSave[AccountingPeriod]()

        when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)
        when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
        when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)
        mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))

        submitAuthorised(Controller.submit(),
          fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
          _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
        }

        verify(mockVatRegistrationService).submitVatFinancials()(any())
      }

      "estimated vat turnover can't be determined" in {
        save4laterExpectsSave[VatReturnFrequency]()
        save4laterExpectsSave[AccountingPeriod]()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
        save4laterReturnsNoViewModel[EstimateVatTurnover]()

        when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)
        when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
        when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)
        mockKeystoreFetchAndGet[Long](EstimateVatTurnoverKey.lastKnownValueKey, Some(0))

        submitAuthorised(Controller.submit(),
          fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
          _ redirectsTo s"$contextRoot/join-flat-rate-scheme"
        }

        verify(mockVatRegistrationService).submitVatFinancials()(any())
      }

    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Quarterly" should {

    "return 303" in {
      save4laterExpectsSave[VatReturnFrequency]()
      save4laterExpectsSave[AccountingPeriod]()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.QUARTERLY)) {
        _ redirectsTo s"$contextRoot/vat-return-periods-end"
      }
    }
  }

}
