/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.business

import fixtures.VatRegistrationFixture
import play.api.test.FakeRequest
import services.mocks.{MockSoleTraderIdService, MockVatRegistrationService}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.supply_workers

import scala.concurrent.Future

class SupplyWorkersControllerSpec extends ControllerSpec
  with FutureAssertions
  with VatRegistrationFixture
  with MockVatRegistrationService
  with MockSoleTraderIdService {

  val view = app.injector.instanceOf[supply_workers]
  val fakeRequest = FakeRequest(controllers.business.routes.SupplyWorkersController.show)

  class Setup {
    object Controller extends SupplyWorkersController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      vatRegistrationServiceMock,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" must {
    "return HTML where getSicAndCompliance returns the view models wih labour already entered" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))
      mockIsTransactor(Future.successful(true))

      callAuthorised(Controller.show) { result =>
        status(result) mustBe OK
      }
    }
    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockIsTransactor(Future.successful(true))

      callAuthorised(Controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submit" must {
    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(Controller.submit, fakeRequest.withFormUrlEncodedBody())(result =>
        status(result) mustBe BAD_REQUEST
      )
    }
    "redirect with company provide workers Yes selected" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness.copy(labourCompliance = Some(complianceWithLabour))))
      mockIsTransactor(Future.successful(true))

      submitAuthorised(Controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      ))(_ redirectsTo controllers.business.routes.WorkersController.show.url)
    }
    "redirect with company provide workers No selected" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockIsTransactor(Future.successful(true))

      submitAuthorised(Controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      ))(_ redirectsTo controllers.business.routes.SupplyWorkersIntermediaryController.show.url)
    }
  }

}
