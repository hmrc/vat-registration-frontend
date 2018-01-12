/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.sicAndCompliance.labour

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.labour.SkilledWorkers
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class SkilledWorkersControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  trait Setup {
    object SkilledWorkersController extends SkilledWorkersController(
      ds,
      mockKeystoreConnector,
      mockAuthConnector,
      mockVatRegistrationService,
      mockSicAndComplianceSrv,
      mockS4LService
    )

    mockGetCurrentProfile()
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.SkilledWorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.SkilledWorkersController.show()}" should {
    "return HTML when there's a Company Provide Skilled Workers model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(SkilledWorkersController.show(), fakeRequest.withFormUrlEncodedBody(
        "skilledWorkersRadio" -> ""
      )) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide skilled workers?")
      }
    }
  }

  s"POST ${sicAndCompliance.labour.routes.SkilledWorkersController.submit()}" should {
    "return 400 with Empty data" in new Setup {

      submitAuthorised(SkilledWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with company provide Skilled workers Yes selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(SkilledWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "skilledWorkersRadio" -> SkilledWorkers.SKILLED_WORKERS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingDetailsController.euGoodsPage().url
      }
    }

    "return 303 with company provide Skilled workers No selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(SkilledWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "skilledWorkersRadio" -> SkilledWorkers.SKILLED_WORKERS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe controllers.routes.TradingDetailsController.euGoodsPage().url
      }
    }
  }
}
