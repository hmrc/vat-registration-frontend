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

package controllers.frs

import fixtures.VatRegistrationFixture
import forms.genericForms.YesOrNoFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
import models.{CurrentProfile, S4LFlatRateScheme}
import models.api.VatFlatRateScheme
import models.view.frs.{BusinessSectorView, RegisterForFrsView}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class RegisterForFrsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends RegisterForFrsController(ds, new YesOrNoFormFactory)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.RegisterForFrsController.show())

  s"GET ${routes.RegisterForFrsController.show()}" should {

    "render page" when {

      "visited for the first time" in {
        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        save4laterReturnsNoViewModel[RegisterForFrsView]()
        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show()) {
          _ includesText "You can use the 16.5% flat rate"
        }
      }

      "user has already answered this question" in {
        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        save4laterReturnsViewModel(RegisterForFrsView(true))()
        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show) {
          _ includesText "You can use the 16.5% flat rate"
        }
      }

      "user's answer has already been submitted to backend" in {
        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        save4laterReturnsNoViewModel[RegisterForFrsView]()
        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)

        callAuthorised(Controller.show) {
          _ includesText "You can use the 16.5% flat rate"
        }
      }

    }
  }

  s"POST ${routes.RegisterForFrsController.submit()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterExpectsSave[RegisterForFrsView]()
      save4laterExpectsSave[BusinessSectorView]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/flat-rate-scheme-join-date")
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterExpectsSave[RegisterForFrsView]()
      save4laterExpectsSave[BusinessSectorView]()
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(VatFlatRateScheme(false).pure)
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockS4LService.fetchAndGet[S4LFlatRateScheme]()(any(), any(), any(), any())).thenReturn(Option.empty.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/check-your-answers")

      verify(mockVatRegistrationService).submitVatFlatRateScheme()(any(), any())
    }
  }
}
