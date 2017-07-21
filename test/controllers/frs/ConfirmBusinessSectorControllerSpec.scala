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
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.frs.BusinessSectorView
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class ConfirmBusinessSectorControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends ConfirmBusinessSectorController(ds, mockConfigConnector) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ConfirmBusinessSectorController.show())

  s"GET ${routes.ConfirmBusinessSectorController.show()}" should {

    "render page" when {

      "visited for the first time" in {
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
        save4laterReturnsNoViewModel[BusinessSectorView]()
        save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
        when(mockConfigConnector.getBusinessSectorDetails(sicCode.id)).thenReturn(validBusinessSectorView)

        callAuthorised(Controller.show()) { result =>
          result includesText "Confirm the company&#x27;s business type"
          result includesText "test business sector"
        }
      }

      "user has already answered this question" in {
        save4laterReturnsViewModel(validBusinessSectorView)()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

        callAuthorised(Controller.show()) { result =>
          result includesText "Confirm the company&#x27;s business type"
          result includesText "test business sector"
        }
      }

      "user's answer has already been submitted to backend" in {
        save4laterReturnsNoViewModel[BusinessSectorView]()
        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

        callAuthorised(Controller.show()) { result =>
          result includesText "Confirm the company&#x27;s business type"
          result includesText "test business sector"
        }
      }

    }
  }

  s"POST ${routes.ConfirmBusinessSectorController.submit()}" should {

    "works with Empty data" in {
      save4laterReturnsViewModel(validBusinessSectorView)()
      save4laterExpectsSave[BusinessSectorView]()
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }

  }

}
