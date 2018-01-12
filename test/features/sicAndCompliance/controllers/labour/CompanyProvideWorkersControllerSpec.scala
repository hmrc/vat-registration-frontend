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
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class CompanyProvideWorkersControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  trait Setup {
    object CompanyProvideWorkersController extends CompanyProvideWorkersController(
      ds,
      mockKeystoreConnector,
      mockAuthConnector,
      mockS4LService,
      mockSicAndComplianceSrv,
      mockVatRegistrationService
    )

    mockGetCurrentProfile()
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()}" should {
    "return HTML where getSicAndCompliance returns the view models wih labour already entered" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(CompanyProvideWorkersController.show()) {result =>
        result includesText "Does the company provide workers to other employers?"
        status(result) mustBe 200
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))

      callAuthorised(CompanyProvideWorkersController.show) {result =>
        result includesText "Does the company provide workers to other employers?"
        status(result) mustBe 200
      }
    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()}" should {
    "return 400 with Empty data" in new Setup {
      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with company provide workers Yes selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour.copy(companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)))))

      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_YES
      ))(_ redirectsTo controllers.sicAndCompliance.labour.routes.WorkersController.show().url)
    }

    "return 303 with company provide workers No selected" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour.copy(companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)))))

      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_NO
      ))(_ redirectsTo controllers.routes.TradingDetailsController.euGoodsPage().url)
    }
  }
}
