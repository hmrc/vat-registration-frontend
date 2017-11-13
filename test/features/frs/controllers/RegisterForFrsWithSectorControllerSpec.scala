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
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class RegisterForFrsWithSectorControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends RegisterForFrsWithSectorController(
      ds,
      new YesOrNoFormFactory,
      mockConfigConnector
    )(
      mockS4LService,
      mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.RegisterForFrsWithSectorController.show())

  s"GET ${routes.RegisterForFrsWithSectorController.show()}" should {
    "render page" when {
      "visited for the first time" in {
        mockGetCurrentProfile()

        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
        save4laterReturnsNoViewModel[BusinessSectorView]()
        save4laterReturnsNoViewModel[MainBusinessActivityView]()
        save4laterReturnsNoViewModel[RegisterForFrsView]()
        save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
        when(mockConfigConnector.getBusinessSectorDetails(sicCode.id)).thenReturn(validBusinessSectorView)

        save4laterReturnsNoViewModel[RegisterForFrsView]()
        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show()) {
          _ includesText "Your flat rate"
        }
      }

      "user's answer has already been submitted to backend" in {
        mockGetCurrentProfile()

        save4laterReturnsNoViewModel[BusinessSectorView]()
        save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
        when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)
        when(mockConfigConnector.getBusinessSectorDetails(sicCode.id)).thenReturn(validBusinessSectorView)

        callAuthorised(Controller.show) {
          _ includesText "Your flat rate"
        }
      }
    }
  }

  s"POST ${routes.RegisterForFrsWithSectorController.submit()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()

      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      save4laterReturnsNoViewModel[RegisterForFrsView]()
      save4laterReturnsViewModel(validBusinessSectorView)()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in {
      mockGetCurrentProfile()

      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      save4laterReturnsNoViewModel[RegisterForFrsView]()

      save4laterReturnsViewModel(validBusinessSectorView)()

      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)
      save4laterExpectsSave[RegisterForFrsView]()
      save4laterExpectsSave[BusinessSectorView]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/flat-rate-scheme-join-date")
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in {
      mockGetCurrentProfile()

      save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
      save4laterReturnsNoViewModel[BusinessSectorView]()
      save4laterExpectsSave[RegisterForFrsView]()
      save4laterExpectsSave[BusinessSectorView]()
      when(mockConfigConnector.getBusinessSectorDetails(sicCode.id)).thenReturn(validBusinessSectorView)
      when(mockS4LService.fetchAndGet[S4LFlatRateScheme]()(any(), any(), any(), any())).thenReturn(Option.empty.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(VatFlatRateScheme(false).pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/check-your-answers")

      verify(mockVatRegistrationService).submitVatFlatRateScheme()(any(), any())
    }
  }
}
