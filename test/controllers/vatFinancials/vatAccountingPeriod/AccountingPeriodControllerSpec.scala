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
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class AccountingPeriodControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends AccountingPeriodController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show())

  s"GET ${vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show()}" should {

    "return HTML when there's a Accounting Period model in S4L" in {
      save4laterReturnsViewModel(AccountingPeriod(""))()

      submitAuthorised(Controller.show(),
        fakeRequest.withFormUrlEncodedBody("accountingPeriodRadio" -> "")) {
        _ includesText "When do you want your VAT Return periods to end?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contain data" in {
      save4laterReturnsNoViewModel[AccountingPeriod]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "When do you want your VAT Return periods to end?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contain no data" in {
      save4laterReturnsNoViewModel[AccountingPeriod]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "When do you want your VAT Return periods to end?"
      }
    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(_ isA 400)
    }
  }

  s"POST ${vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.submit()} with any accounting period selected" should {
    "redirect to mandatory start date page" when {

      "voluntary registration is no" in {
        forAll(Seq(AccountingPeriod.FEB_MAY_AUG_NOV, AccountingPeriod.JAN_APR_JUL_OCT, AccountingPeriod.MAR_JUN_SEP_DEC)) {
          accountingPeriod =>
            save4laterReturnsViewModel(VoluntaryRegistration.no)()
            save4laterExpectsSave[AccountingPeriod]()
            when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
            when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)

            submitAuthorised(Controller.submit(),
              fakeRequest.withFormUrlEncodedBody("accountingPeriodRadio" -> accountingPeriod)) {
              _ redirectsTo s"$contextRoot/vat-start-date"
            }
        }
      }

      "voluntary registration is yes" in {
        forAll(Seq(AccountingPeriod.FEB_MAY_AUG_NOV, AccountingPeriod.JAN_APR_JUL_OCT, AccountingPeriod.MAR_JUN_SEP_DEC)) {
          accountingPeriod =>
            save4laterReturnsViewModel(VoluntaryRegistration.yes)()
            save4laterExpectsSave[AccountingPeriod]()
            when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
            when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)

            submitAuthorised(Controller.submit(),
              fakeRequest.withFormUrlEncodedBody("accountingPeriodRadio" -> accountingPeriod)) {
              _ redirectsTo s"$contextRoot/what-do-you-want-your-vat-start-date-to-be"
            }
        }
      }

      "no voluntary registration view model exists" in {
        forAll(Seq(AccountingPeriod.FEB_MAY_AUG_NOV, AccountingPeriod.JAN_APR_JUL_OCT, AccountingPeriod.MAR_JUN_SEP_DEC)) {
          accountingPeriod =>
            when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
            save4laterExpectsSave[AccountingPeriod]()
            save4laterReturnsNoViewModel[VoluntaryRegistration]()
            when(mockVatRegistrationService.submitVatFinancials()(any())).thenReturn(validVatFinancials.pure)
            when(mockVatRegistrationService.conditionalDeleteElement(any(),any())(any())).thenReturn(().pure)

            submitAuthorised(Controller.submit(),
              fakeRequest.withFormUrlEncodedBody("accountingPeriodRadio" -> accountingPeriod)) {
              _ redirectsTo s"$contextRoot/what-do-you-want-your-vat-start-date-to-be"
            }
        }
      }

    }

  }
}
