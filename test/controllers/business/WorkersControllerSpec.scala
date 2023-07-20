/*
 * Copyright 2023 HM Revenue & Customs
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

import featuretoggle.FeatureSwitch.FeatureSwitch
import fixtures.VatRegistrationFixture
import models.api.{NonUkNonEstablished, UkCompany}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{BusinessServiceMock, MockVatRegistrationService}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.workers

import scala.concurrent.Future

class WorkersControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with BusinessServiceMock with MockVatRegistrationService {

  trait Setup {
    val view = app.injector.instanceOf[workers]
    val controller: WorkersController = new WorkersController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      vatRegistrationServiceMock,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.business.routes.WorkersController.show)

    def verifyRedirectLocation(featureSwitchFn: FeatureSwitch => Unit, resolvedLocation: Call): Unit = {
      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      ))(_ redirectsTo resolvedLocation.url)
    }
  }

  s"GET ${controllers.business.routes.WorkersController.show}" should {
    "return OK when there's a Workers model in backend" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))
      mockIsTransactor(Future.successful(true))

      callAuthorised(controller.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockIsTransactor(Future.successful(true))

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.business.routes.WorkersController.submit}" should {
    "return BAD_REQUEST with Empty data" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe BAD_REQUEST
      }
    }
    "redirect to the Imports of Exports or OBI for UkCompany" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockIsTransactor(Future.successful(true))
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      ))(_ redirectsTo controllers.routes.TaskListController.show.url)
    }

    "redirect to the Turnover or OBI for NonUkCompany" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockIsTransactor(Future.successful(true))
      mockPartyType(Future.successful(NonUkNonEstablished))

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      ))(_ redirectsTo controllers.routes.TaskListController.show.url)
    }
  }
}