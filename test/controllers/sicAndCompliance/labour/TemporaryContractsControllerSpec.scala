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
import models.view.sicAndCompliance.labour.TemporaryContracts
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

class TemporaryContractsControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object TemporaryContractsController extends TemporaryContractsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.labour.routes.TemporaryContractsController.show())

  s"GET ${sicAndCompliance.labour.routes.TemporaryContractsController.show()}" should {

    "return HTML when there's a Temporary Contracts model in S4L" in {
      val temporaryContracts = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)

      when(mockS4LService.fetchAndGet[TemporaryContracts]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(temporaryContracts)))

      submitAuthorised(TemporaryContractsController.show(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers on temporary contracts?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[TemporaryContracts]()
        (Matchers.eq(S4LKey[TemporaryContracts]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TemporaryContractsController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Does the company provide workers on temporary contracts?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[TemporaryContracts]()
      (Matchers.eq(S4LKey[TemporaryContracts]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(TemporaryContractsController.show) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Does the company provide workers on temporary contracts?")
    }
  }

  s"POST ${sicAndCompliance.labour.routes.TemporaryContractsController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${sicAndCompliance.labour.routes.TemporaryContractsController.submit()} with Temporary Contracts Yes selected" should {

    "return 303" in {
      val returnCacheMapTemporaryContracts = CacheMap("", Map("" -> Json.toJson(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES))))

      when(mockS4LService.saveForm[TemporaryContracts]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapTemporaryContracts))

      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/compliance/skilled-workers"
      }

    }
  }

  s"POST ${sicAndCompliance.labour.routes.TemporaryContractsController.submit()} with TemporaryContracts No selected" should {

    "return 303" in {
      val returnCacheMapTemporaryContracts = CacheMap("", Map("" -> Json.toJson(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO))))

      when(mockS4LService.saveForm[TemporaryContracts]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapTemporaryContracts))

      submitAuthorised(TemporaryContractsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "temporaryContractsRadio" -> TemporaryContracts.TEMP_CONTRACTS_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }
}