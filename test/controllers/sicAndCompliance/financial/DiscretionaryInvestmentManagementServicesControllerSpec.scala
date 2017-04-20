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
import models.view.sicAndCompliance.financial.{ChargeFees, DiscretionaryInvestmentManagementServices}
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

class DiscretionaryInvestmentManagementServicesControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object DiscretionaryInvestmentManagementServicesController extends DiscretionaryInvestmentManagementServicesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.DiscretionaryInvestmentManagementServicesController.show())

  s"GET ${routes.DiscretionaryInvestmentManagementServicesController.show()}" should {

    "return HTML when there's a DiscretionaryInvestmentManagementServices model in S4L" in {
      val discretionaryInvestmentManagementServices = DiscretionaryInvestmentManagementServices(true)

      when(mockS4LService.fetchAndGet[DiscretionaryInvestmentManagementServices]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(discretionaryInvestmentManagementServices)))

      AuthBuilder.submitWithAuthorisedUser(DiscretionaryInvestmentManagementServicesController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> ""
      )) {
        _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[DiscretionaryInvestmentManagementServices]()
        (Matchers.eq(S4LKey[DiscretionaryInvestmentManagementServices]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(DiscretionaryInvestmentManagementServicesController.show, mockAuthConnector) {
       _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[DiscretionaryInvestmentManagementServices]()
      (Matchers.eq(S4LKey[DiscretionaryInvestmentManagementServices]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(DiscretionaryInvestmentManagementServicesController.show, mockAuthConnector) {
     _ includesText "Does the company provide discretionary investment management services, or introduce clients to companies who do?"
    }
  }

  s"POST ${routes.DiscretionaryInvestmentManagementServicesController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(DiscretionaryInvestmentManagementServicesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.DiscretionaryInvestmentManagementServicesController.submit()} with Provide Discretionary Investment Management Services Yes selected" should {

    "return 303" in {
      val returnCacheMapDiscretionaryInvestmentManagementServices = CacheMap("", Map("" -> Json.toJson(DiscretionaryInvestmentManagementServices(true))))

      when(mockS4LService.saveForm[DiscretionaryInvestmentManagementServices]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapDiscretionaryInvestmentManagementServices))

      AuthBuilder.submitWithAuthorisedUser(DiscretionaryInvestmentManagementServicesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> "true"
      )) {
        response =>
          response redirectsTo s"$contextRoot/company-bank-account"
      }

    }
  }

  s"POST ${routes.ChargeFeesController.submit()} with Provide Discretionary Investment Management Services No selected" should {

    "return 303" in {
      val returnCacheMapDiscretionaryInvestmentManagementServices = CacheMap("", Map("" -> Json.toJson(DiscretionaryInvestmentManagementServices(false))))

      when(mockS4LService.saveForm[DiscretionaryInvestmentManagementServices]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapDiscretionaryInvestmentManagementServices))

      AuthBuilder.submitWithAuthorisedUser(DiscretionaryInvestmentManagementServicesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "discretionaryInvestmentManagementServicesRadio" -> "false"
      )) {
        response =>
          response redirectsTo s"$contextRoot/involved-in-leasing-vehicles-or-equipment"
      }

    }
  }
}