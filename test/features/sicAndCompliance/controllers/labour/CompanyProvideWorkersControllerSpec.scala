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

package controllers.sicAndCompliance.labour

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.{CurrentProfile, S4LVatSicAndCompliance}
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CompanyProvideWorkersControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object CompanyProvideWorkersController extends CompanyProvideWorkersController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()}" should {
    "return HTML when there's a Company Provide Workers model in S4L" in {
      save4laterReturnsViewModel(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO))()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(CompanyProvideWorkersController.show(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> ""
      )) {
        _ includesText "Does the company provide workers to other employers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[CompanyProvideWorkers]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(Future.successful(validVatScheme))
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      callAuthorised(CompanyProvideWorkersController.show) {
        _ includesText "Does the company provide workers to other employers?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[CompanyProvideWorkers]()
    when(mockVatRegistrationService.getVatScheme()(any(), any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
    when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(currentProfile)))
      callAuthorised(CompanyProvideWorkersController.show) {
        _ includesText "Does the company provide workers to other employers?"
      }
    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with company provide workers Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturns(S4LVatSicAndCompliance())

      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_YES
      ))(_ redirectsTo s"$contextRoot/how-many-workers-does-company-provide-at-one-time")
    }

    "return 303 with company provide workers No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturns(S4LVatSicAndCompliance())

      submitAuthorised(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_NO
      ))(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }
  }
}