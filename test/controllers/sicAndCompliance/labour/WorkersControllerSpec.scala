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
import models.view.sicAndCompliance.labour.Workers
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class WorkersControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object WorkersController extends WorkersController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.WorkersController.show())

  s"GET ${sicAndCompliance.labour.routes.WorkersController.show()}" should {

    "return HTML when there's a Workers model in S4L" in {
      val workers = Workers(5)

      when(mockS4LService.fetchAndGet[Workers]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(workers)))

      AuthBuilder.submitWithAuthorisedUser(WorkersController.show(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      )) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("How many workers does the company provide at any one time?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[Workers]()
        (Matchers.eq(S4LKey[Workers]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(WorkersController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("How many workers does the company provide at any one time?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[Workers]()
      (Matchers.eq(S4LKey[Workers]), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(WorkersController.show) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("How many workers does the company provide at any one time?")
    }
  }

  s"POST ${sicAndCompliance.labour.routes.WorkersController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(WorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }
    }
  }

  s"POST ${sicAndCompliance.labour.routes.WorkersController.submit()} with less than 8 workers entered" should {

    "return 303" in {
      val returnCacheMapWorkers = CacheMap("", Map("" -> Json.toJson(Workers(5))))

      when(mockS4LService.saveForm[Workers]
        (any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapWorkers))

      AuthBuilder.submitWithAuthorisedUser(WorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      )) {
        result =>
          result redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }

  s"POST ${sicAndCompliance.labour.routes.WorkersController.submit()} with 8 or more workers entered" should {

    "return 303" in {
      val returnCacheMapWorkers = CacheMap("", Map("" -> Json.toJson(Workers(8))))

      when(mockS4LService.saveForm[Workers]
        (any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapWorkers))

      AuthBuilder.submitWithAuthorisedUser(WorkersController.submit(), fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "8"
      )) {
        result =>
          result redirectsTo s"$contextRoot/compliance/temporary-contracts"
      }
    }
  }

}