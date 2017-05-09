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

package controllers.sicAndCompliance.financial

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.financial.ManageAdditionalFunds
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

class ManageAdditionalFundsControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object ManageAdditionalFundsController extends ManageAdditionalFundsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ManageAdditionalFundsController.show())

  s"GET ${routes.ManageAdditionalFundsController.show()}" should {

    "return HTML when there's a Manage Additional Funds model in S4L" in {
      val manageAdditionalFunds = ManageAdditionalFunds(true)

      when(mockS4LService.fetchAndGet[ManageAdditionalFunds]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(manageAdditionalFunds)))

      AuthBuilder.submitWithAuthorisedUser(ManageAdditionalFundsController.show(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> ""
      )) {
        _ includesText "Does the company manage any funds that are not included in this list?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[ManageAdditionalFunds]()
        (Matchers.eq(S4LKey[ManageAdditionalFunds]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(ManageAdditionalFundsController.show) {
       _ includesText "Does the company manage any funds that are not included in this list?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[ManageAdditionalFunds]()
      (Matchers.eq(S4LKey[ManageAdditionalFunds]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(ManageAdditionalFundsController.show) {
     _ includesText "Does the company manage any funds that are not included in this list?"
    }
  }

  s"POST ${routes.ManageAdditionalFundsController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.ManageAdditionalFundsController.submit()} with Manage Additional Funds Yes selected" should {

    "return 303" in {
      val returnCacheMapManageAdditionalFunds = CacheMap("", Map("" -> Json.toJson(ManageAdditionalFunds(true))))

      when(mockS4LService.saveForm[ManageAdditionalFunds]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapManageAdditionalFunds))

      AuthBuilder.submitWithAuthorisedUser(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> "true"
      )) {
        response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }

  s"POST ${routes.ManageAdditionalFundsController.submit()} with Manage Additional Funds No selected" should {

    "return 303" in {
      val returnCacheMapManageAdditionalFunds = CacheMap("", Map("" -> Json.toJson(ManageAdditionalFunds(false))))

      when(mockS4LService.saveForm[ManageAdditionalFunds]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapManageAdditionalFunds))

      AuthBuilder.submitWithAuthorisedUser(ManageAdditionalFundsController.submit(), fakeRequest.withFormUrlEncodedBody(
        "manageAdditionalFundsRadio" -> "false"
      )) {
        response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }
}