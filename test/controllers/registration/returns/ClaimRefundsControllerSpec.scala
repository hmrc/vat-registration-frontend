/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.returns

import _root_.models._
import featureswitch.core.config.{FeatureSwitching, NorthernIrelandProtocol}
import fixtures.VatRegistrationFixture
import models.api.{NETP, UkCompany}
import models.api.returns.Returns
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.returns.claim_refunds_view

import scala.concurrent.Future

class ClaimRefundsControllerSpec extends ControllerSpec
  with VatRegistrationFixture
  with TimeServiceMock
  with FutureAssertions
  with FeatureSwitching {

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile)) {
    val testController = new ClaimRefundsController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockReturnsService,
      mockVatRegistrationService,
      app.injector.instanceOf[claim_refunds_view]
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
  }

  val emptyReturns: Returns = Returns()
  val voluntary = true
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.registration.returns.routes.ClaimRefundsController.show)

  "show" should {
    "return OK when returns are found" in new Setup {
      when(mockReturnsService.getReturns(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not found" in new Setup {
      when(mockReturnsService.getReturns(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submit" should {
    "return SEE_OTHER when they expect to reclaim more vat than they charge and redirect to VAT Start Page - mandatory flow" in new Setup {
      disable(NorthernIrelandProtocol)
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))


      when(mockReturnsService.isVoluntary(any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/mandatory-vat-start-date")
      }
    }

    "return SEE_OTHER when they don't expect to reclaim more vat than they charge and redirect to VAT Start Page - voluntarily flow" in new Setup {
      disable(NorthernIrelandProtocol)
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(false))))

      when(mockReturnsService.isVoluntary(any(), any()))
        .thenReturn(Future.successful(voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "return SEE_OTHER when partyType is NETP then redirect to send goods to overseas pages" in new Setup {
      enable(NorthernIrelandProtocol)
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(NETP))
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))
      when(mockReturnsService.isVoluntary(any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/send-goods-overseas")
      }
    }

    "return SEE_OTHER when they expect to reclaim more vat than they charge and redirect to NIP pages" in new Setup {
      enable(NorthernIrelandProtocol)
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))
      when(mockReturnsService.isVoluntary(any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/sell-or-move-nip")
      }
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> ""
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST if the option selected is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "INVALID-OPTION"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

}
