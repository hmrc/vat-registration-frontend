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

import java.time.LocalDate

import connectors.KeystoreConnector
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import forms.vatFinancials.vatAccountingPeriod.VatReturnFrequencyForm
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.{VatEligibilityChoice, VatExpectedThresholdPostIncorp, VatServiceEligibility}
import models.{CurrentProfile, S4LVatFinancials}
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class VatReturnFrequencyControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends VatReturnFrequencyController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())

  val expectedThresholdDate = LocalDate.of(2017, 6, 21)
  val expectedThreshold = Some(VatExpectedThresholdPostIncorp(expectedOverThresholdSelection = true, Some(expectedThresholdDate)))

  val mandatoryEligibilityThreshold: Option[VatServiceEligibility] = Some(
    validServiceEligibility(
      VatEligibilityChoice.NECESSITY_OBLIGATORY,
      None,
      expectedThreshold
    )
  )

  s"GET ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show()}" should {

    "return HTML when there's a Vat Return Frequency model in S4L" in {
      mockGetCurrentProfile()

      save4laterReturnsViewModel(VatReturnFrequency(VatReturnFrequency.MONTHLY))()

      submitAuthorised(Controller.show(),
        fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> "")) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      mockGetCurrentProfile()
      save4laterReturnsNoViewModel[VatReturnFrequency]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      mockGetCurrentProfile()

      save4laterReturnsNoViewModel[VatReturnFrequency]()
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "How often do you want to submit VAT Returns?"
      }
    }
  }


  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Empty data" should {
    "return 400" in {
      mockGetCurrentProfile()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Monthly" should {
    "redirect to mandatory start date page" when {
      "voluntary registration is no" in {

        when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

        mockGetCurrentProfile()

        save4laterReturnsViewModel(VoluntaryRegistration.no)()
        save4laterExpectsSave[VatReturnFrequency]()
        when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
        save4laterReturns(S4LVatFinancials())

        submitAuthorised(Controller.submit(),
          fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
          _ redirectsTo s"$contextRoot/vat-start-date"
        }
      }
      "voluntary registration is yes" in {
        when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = Some(validServiceEligibility()))))

        mockGetCurrentProfile()

        save4laterReturnsViewModel(VoluntaryRegistration.yes)()
        save4laterExpectsSave[VatReturnFrequency]()
        when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
        save4laterReturns(S4LVatFinancials())

        submitAuthorised(Controller.submit(),
          fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
          _ redirectsTo s"$contextRoot/what-do-you-want-your-vat-start-date-to-be"
        }
      }


      "no eligiblity choice exists" in {

        mockGetCurrentProfile()

        when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)
        save4laterExpectsSave[VatReturnFrequency]()
        save4laterReturnsNoViewModel[VoluntaryRegistration]()
        when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
        save4laterReturns(S4LVatFinancials())

        intercept[RuntimeException] {
          submitAuthorised(Controller.submit(),
            fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY)) {
            _ redirectsTo s"$contextRoot/vat-start-date"
          }
        }
      }

    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Quarterly" should {
    "return 303" in {
      mockGetCurrentProfile()

      save4laterExpectsSave[VatReturnFrequency]()
      save4laterExpectsSave[AccountingPeriod]()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody(VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.QUARTERLY)) {
        _ redirectsTo s"$contextRoot/vat-return-periods-end"
      }
    }
  }

}
