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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.financial.{AdviceOrConsultancy, ChargeFees}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ChargeFeesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object ChargeFeesController extends ChargeFeesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  val fakeRequest = FakeRequest(routes.ChargeFeesController.show())

  s"GET ${routes.ChargeFeesController.show()}" should {

    "return HTML when there's a Charge Fees model in S4L" in {
      save4laterReturnsViewModel(ChargeFees(true))()

      submitAuthorised(ChargeFeesController.show(), fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> ""
      )) {
        _ includesText "Does the company charge fees for introducing clients to financial service providers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[ChargeFees]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(ChargeFeesController.show) {
       _ includesText "Does the company charge fees for introducing clients to financial service providers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[ChargeFees]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(ChargeFeesController.show) {
        _ includesText "Does the company charge fees for introducing clients to financial service providers?"
      }
    }
  }

  s"POST ${routes.ChargeFeesController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(ChargeFeesController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with charge fees Yes" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      save4laterExpectsSave[AdviceOrConsultancy]()

      submitAuthorised(ChargeFeesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> "true"
      )) {
        response =>
          response redirectsTo s"$contextRoot/does-additional-work-when-introducing-client-to-financial-service-provider"
      }
    }

    "return 303 with charge fees No" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      save4laterExpectsSave[AdviceOrConsultancy]()

      submitAuthorised(ChargeFeesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "chargeFeesRadio" -> "false"
      )) {
        response =>
          response redirectsTo s"$contextRoot/does-additional-work-when-introducing-client-to-financial-service-provider"
      }
    }
  }
}