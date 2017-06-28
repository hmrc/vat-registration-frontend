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
import models.api.VatFlatRateScheme
import models.view.frs.JoinFrsView
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class JoinFrsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends JoinFrsController(ds, new YesOrNoFormFactory)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.JoinFrsController.show())

  s"GET ${routes.JoinFrsController.show()}" should {

    "render page" when {

      "visited for the first time" in {
        save4laterReturnsNoViewModel[JoinFrsView]()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show()) {
          _ includesText "Do you want to join the Flat Rate Scheme?"
        }
      }

      "user has already answered this question" in {
        save4laterReturnsViewModel(JoinFrsView(true))()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show) {
          _ includesText "Do you want to join the Flat Rate Scheme?"
        }
      }

      "user's answer has already been submitted to backend" in {
        save4laterReturnsNoViewModel[JoinFrsView]()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

        callAuthorised(Controller.show) {
          _ includesText "Do you want to join the Flat Rate Scheme?"
        }
      }

    }

  }

  s"POST ${routes.JoinFrsController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.JoinFrsController.submit()} with Join Flat Rate Scheme selected Yes" should {

    "return 303" in {
      save4laterExpectsSave[JoinFrsView]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/spends-less-including-vat-on-goods")
    }
  }

  s"POST ${routes.JoinFrsController.submit()} with Join Flat Rate Scheme selected No" should {

    "redirect to the welcome page" in {
      when(mockS4LService.clear()(any())).thenReturn(validHttpResponse.pure)
      save4laterExpectsSave[JoinFrsView]()
      when(mockVatRegistrationService.submitFrsAnswers()(any())).thenReturn(VatFlatRateScheme(false).pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/check-your-answers")

      verify(mockVatRegistrationService).submitFrsAnswers()(any())
    }
  }

}
