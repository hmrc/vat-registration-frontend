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

import builders.AuthBuilder
import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CompanyProvideWorkersControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object CompanyProvideWorkersController extends CompanyProvideWorkersController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()}" should {

    "return HTML when there's a Company Provide Workers model in S4L" in {
      val companyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)

      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(companyProvideWorkers)))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.show(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> ""
      )) {
        _ includesText "Does the company provide workers to other employers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()
        (Matchers.eq(S4LKey[CompanyProvideWorkers]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(CompanyProvideWorkersController.show) {
        _ includesText "Does the company provide workers to other employers?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(Matchers.eq(S4LKey[CompanyProvideWorkers]), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(CompanyProvideWorkersController.show) {
      _ includesText "Does the company provide workers to other employers?"
    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with company provide workers Yes selected" should {

    "return 303" in {
      val returnCacheMapCompanyProvideWorkers = CacheMap("", Map("" -> Json.toJson(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES))))

      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[CompanyProvideWorkers](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapCompanyProvideWorkers))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_YES
      ))(_ redirectsTo s"$contextRoot/compliance/workers")

    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with company provide workers No selected" should {

    "return 303" in {
      val returnCacheMapCompanyProvideWorkers = CacheMap("", Map("" -> Json.toJson(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO))))

      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[CompanyProvideWorkers](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapCompanyProvideWorkers))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_NO
      ))(_ redirectsTo s"$contextRoot/company-bank-account")

    }
  }
}