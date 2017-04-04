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

package controllers.userJourney.sicAndCompliance.labour

import builders.AuthBuilder
import controllers.userJourney.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CompanyProvideWorkersControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object CompanyProvideWorkersController extends CompanyProvideWorkersController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()}" should {

    "return HTML when there's a Company Provide Workers model in S4L" in {
      val companyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)

      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(companyProvideWorkers)))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers to other employers?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()
        (Matchers.eq(S4LKey[CompanyProvideWorkers]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(CompanyProvideWorkersController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers to other employers?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()
      (Matchers.eq(S4LKey[CompanyProvideWorkers]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(CompanyProvideWorkersController.show, mockAuthConnector) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Does the company provide workers to other employers?")
    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with company provide workers Yes selected" should {

    "return 303" in {
      val returnCacheMapCompanyProvideWorkers = CacheMap("", Map("" -> Json.toJson(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES))))

      when(mockS4LService.saveForm[CompanyProvideWorkers]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCompanyProvideWorkers))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/compliance/workers"
      }

    }
  }

  s"POST ${sicAndCompliance.labour.routes.CompanyProvideWorkersController.submit()} with company provide workers No selected" should {

    "return 303" in {
      val returnCacheMapCompanyProvideWorkers = CacheMap("", Map("" -> Json.toJson(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO))))

      when(mockS4LService.saveForm[CompanyProvideWorkers]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapCompanyProvideWorkers))

      AuthBuilder.submitWithAuthorisedUser(CompanyProvideWorkersController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "companyProvideWorkersRadio" -> CompanyProvideWorkers.PROVIDE_WORKERS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }
}