/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.sicandcompliance

import fixtures.VatRegistrationFixture
import mocks.SicAndComplianceServiceMock
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.labour.workers

import scala.concurrent.Future

class WorkersControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with SicAndComplianceServiceMock {

  trait Setup {
    val view = app.injector.instanceOf[workers]
    val controller: WorkersController = new WorkersController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockSicAndComplianceService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${controllers.registration.sicandcompliance.routes.WorkersController.show()}" should {
    "return OK when there's a Workers model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.registration.sicandcompliance.routes.WorkersController.submit()}" should {
    val fakeRequest = FakeRequest(controllers.registration.sicandcompliance.routes.WorkersController.show())

    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe BAD_REQUEST
      }
    }
    "redirect to the trading name page" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      )) {
        result =>
          result redirectsTo controllers.registration.business.routes.TradingNameController.show().url
      }
    }
  }

}
