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

package controllers.vatTradingDetails.vatChoice

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class VoluntaryRegistrationReasonControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestVoluntaryRegistrationReasonController
    extends VoluntaryRegistrationReasonController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationReasonController.show())

  s"GET ${routes.VoluntaryRegistrationReasonController.show()}" should {

    "return HTML Voluntary Registration Reason page with no Selection" in {
      val voluntaryRegistrationReason = VoluntaryRegistrationReason("")

      save4laterReturnsViewModel(voluntaryRegistrationReason)()

      submitAuthorised(TestVoluntaryRegistrationReasonController.show(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> ""
      )) {
        _ includesText "Which one of the following apply to the company?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[VoluntaryRegistrationReason]()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVoluntaryRegistrationReasonController.show) {
        _ includesText "Which one of the following apply to the company?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[VoluntaryRegistrationReason]()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestVoluntaryRegistrationReasonController.show) {
        _ includesText "Which one of the following apply to the company?"
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Sells" should {

    "return 303" in {
      save4laterExpectsSave[VoluntaryRegistrationReason]()

      submitAuthorised(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.SELLS
      ))(_ redirectsTo s"$contextRoot/who-is-registering-the-company-for-vat")
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Intends to sell" should {

    "return 303" in {
      save4laterExpectsSave[VoluntaryRegistrationReason]()

      submitAuthorised(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.SELLS
      ))(_ redirectsTo s"$contextRoot/who-is-registering-the-company-for-vat")
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration selected No" should {

    "redirect to the welcome page" in {
      when(mockS4LService.clear()(any())).thenReturn(Future.successful(validHttpResponse))
      save4laterExpectsSave[VoluntaryRegistrationReason]()
      when(mockVatRegistrationService.deleteVatScheme()(any())).thenReturn(Future.successful(()))

      submitAuthorised(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.NEITHER
      ))(_ redirectsTo contextRoot)
    }
  }

}
