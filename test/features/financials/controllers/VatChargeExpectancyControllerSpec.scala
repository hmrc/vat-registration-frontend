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

package controllers.vatFinancials

import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatFinancials.VatChargeExpectancy
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class VatChargeExpectancyControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends VatChargeExpectancyController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(vatFinancials.routes.VatChargeExpectancyController.show())

  s"GET ${vatFinancials.routes.VatChargeExpectancyController.show()}" should {

    "return HTML when there's a Vat Charge Expectancy model in S4L" in {
      save4laterReturnsViewModel(VatChargeExpectancy.yes)()

      mockGetCurrentProfile()

      callAuthorised(Controller.show()) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[VatChargeExpectancy]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(validVatScheme.pure)

      mockGetCurrentProfile()

      callAuthorised(Controller.show) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[VatChargeExpectancy]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(emptyVatScheme.pure)
      mockGetCurrentProfile()

      callAuthorised(Controller.show) {
        _ includesText "Do you expect to reclaim more VAT than you charge?"
      }
    }
  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Empty data" should {
    "return 400" in {
      mockGetCurrentProfile()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected Yes" should {
    "return 303" in {
      save4laterExpectsSave[VatChargeExpectancy]()

      mockGetCurrentProfile()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_YES)) {
        _ redirectsTo s"$contextRoot/how-often-do-you-want-to-submit-vat-returns"
      }
    }

  }

  s"POST ${vatFinancials.routes.VatChargeExpectancyController.submit()} with Vat Charge Expectancy selected No" should {
    "return 303" in {
      save4laterExpectsSave[VatChargeExpectancy]()
      save4laterExpectsSave[VatReturnFrequency]()

      mockGetCurrentProfile()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("vatChargeRadio" -> VatChargeExpectancy.VAT_CHARGE_NO)) {
        _ redirectsTo s"$contextRoot/vat-return-periods-end"
      }
    }
  }
}
