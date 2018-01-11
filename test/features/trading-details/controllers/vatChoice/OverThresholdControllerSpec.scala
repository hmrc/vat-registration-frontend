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

package controllers.vatTradingDetails.vatChoice

import java.time.LocalDate

import common.Now
import fixtures.VatRegistrationFixture
import forms.vatTradingDetails.vatChoice.OverThresholdFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys._
import models.external.IncorporationInfo
import models.view.vatTradingDetails.vatChoice.OverThresholdView
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.exceptions.TestFailedException
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class OverThresholdControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val today: LocalDate = LocalDate.of(2017, 6, 30)

  val overThresholdFormFactory = new OverThresholdFormFactory(Now[LocalDate](today))

  object TestOverThresholdController extends OverThresholdController(
    overThresholdFormFactory,
    ds,
    mockAuthConnector,
    mockKeystoreConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(routes.OverThresholdController.show())

  s"GET ${routes.OverThresholdController.show()}" should {
    "returnException if no IncorporationInfo Date present" in {
      val overThreshold = OverThresholdView(true, Some(LocalDate.of(2017, 6, 30)))

      save4laterReturnsViewModel(overThreshold)()
      val testIncorporationInfoWithOutDate = testIncorporationInfo.copy(statusEvent = testIncorporationInfo.statusEvent.copy(incorporationDate = None))

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfoWithOutDate))

      assertThrows[TestFailedException]{
        callAuthorised(TestOverThresholdController.show)(_ =>fail())
      }
    }

    "return HTML when there's a over threshold view in S4L" in {
      val overThreshold = OverThresholdView(true, Some(LocalDate.of(2017, 6, 30)))

      save4laterReturnsViewModel(overThreshold)()

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      callAuthorised(TestOverThresholdController.show) {
        _ includesText "VAT taxable turnover gone over"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[OverThresholdView]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      callAuthorised(TestOverThresholdController.show) {
        _ includesText "VAT taxable turnover gone over"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[OverThresholdView]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      callAuthorised(TestOverThresholdController.show) {
        _ includesText "VAT taxable turnover gone over"
      }
    }
  }

  s"POST ${routes.OverThresholdController.submit()}" should {
    "return Exception When Incorporation date is empty" in {
      val testIncorporationInfoWithOutDate = testIncorporationInfo.copy(statusEvent = testIncorporationInfo.statusEvent.copy(incorporationDate = None))

      mockGetCurrentProfile()
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfoWithOutDate))
      assertThrows[TestFailedException]{
        submitAuthorised(TestOverThresholdController.submit(), fakeRequest.withFormUrlEncodedBody()) {
          (_ =>fail())
        }
      }
    }

    "return 400 when no data posted" in {

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))
      submitAuthorised(
        TestOverThresholdController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      submitAuthorised(
        TestOverThresholdController.submit(), fakeRequest.withFormUrlEncodedBody(
          "overThresholdRadio" -> "true",
          "overThreshold.month" -> "",
          "overThreshold.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with valid data - yes selected" in {
      save4laterExpectsSave[OverThresholdView]()

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      submitAuthorised(TestOverThresholdController.submit(), fakeRequest.withFormUrlEncodedBody(
        "overThresholdRadio" -> "true",
        "overThreshold.month" -> "6",
        "overThreshold.year" -> "2017"
      )) {
        _ redirectsTo s"$contextRoot/check-confirm-answers"
      }
    }

    "return 303 with valid data - no selected" in {
      save4laterExpectsSave[OverThresholdView]()

      mockGetCurrentProfile()

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      submitAuthorised(TestOverThresholdController.submit(), fakeRequest.withFormUrlEncodedBody(
        "overThresholdRadio" -> "false"
      )) {
        _ redirectsTo s"$contextRoot/check-confirm-answers"
      }
    }

  }
}
