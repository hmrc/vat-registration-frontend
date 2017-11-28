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

package controllers.vatTradingDetails.vatChoice

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import cats.data.OptionT
import common.Now
import fixtures.VatRegistrationFixture
import forms.vatTradingDetails.vatChoice.StartDateFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class StartDateControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val today: LocalDate = LocalDate.of(2017, 3, 21)
  val validPastRegIncorpDate : LocalDate = LocalDate.of(2017, 2, 21)
  val veryOldRegIncorpDate : LocalDate = LocalDate.of(2012, 12, 31)

  val startDateFormFactory = new StartDateFormFactory(mockDateService, Now[LocalDate](today))

  object TestStartDateController extends StartDateController(
    startDateFormFactory,
    mockPPService,
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService) {
    implicit val fixedToday = Now[LocalDate](today)
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.StartDateController.show()}" should {
    "return HTML when there's a start date in S4L and the company is incorporated" in {
      val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

      save4laterReturnsViewModel(startDate)()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      mockGetCurrentProfile()

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML for S4L of company registration date and the incorp date has changed from their selection" in {
      val startDate = StartDateView(StartDateView.COMPANY_REGISTRATION_DATE, Some(LocalDate.of(2017, 3, 21)))

      save4laterReturnsViewModel(startDate)()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(
          currentProfile().copy(incorporationDate = Some(validPastRegIncorpDate)))
        ))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's a start date in S4L and the company is not incorporated" in {
      val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

      save4laterReturnsViewModel(startDate)()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentNonincorpProfile)))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data, with an incorporated company" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      mockGetCurrentProfile()

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data, with a non incorporated company" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentNonincorpProfile)))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data with an incorporated company" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      mockGetCurrentProfile()

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data with a non incorporated company" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentNonincorpProfile)))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }
  }

  s"POST ${routes.StartDateController.submit()}" should {
    "return 400 when no data posted" in {

      mockGetCurrentProfile()
      submitAuthorised(
        TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {

      mockGetCurrentProfile()
      submitAuthorised(
        TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> StartDateView.SPECIFIC_DATE,
          "startDate.day" -> "1",
          "startDate.month" -> "",
          "startDate.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when the company was incorporated and the specific date is 3 months in the future" in {

      mockGetCurrentProfile()

      submitAuthorised(
        TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> StartDateView.SPECIFIC_DATE,
          "startDate.day" -> "31",
          "startDate.month" -> "7",
          "startDate.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when the company was incorporated and the specific date is 4 years in the past" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(
          currentProfile().copy(incorporationDate = Some(veryOldRegIncorpDate)))
        ))

      submitAuthorised(
        TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> StartDateView.SPECIFIC_DATE,
          "startDate.day" -> "1",
          "startDate.month" -> "12",
          "startDate.year" -> "2012"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with valid data" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)

      mockGetCurrentProfile()

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.COMPANY_REGISTRATION_DATE,
        "startDate.day" -> "21",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/business-bank-account"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }

    "return 303 with valid data BUSINESS_START_DATE" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)

      mockGetCurrentProfile()

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.BUSINESS_START_DATE
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/business-bank-account"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }

    "return 303 with StartDate having a specific date whilst the company is incorporated" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))
      when(mockDateService.addWorkingDays(ArgumentMatchers.eq(today), anyInt())).thenReturn(today.plus(2, DAYS))
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)

  mockGetCurrentProfile(Some(
    currentProfile().copy(incorporationDate = Some(validPastRegIncorpDate))))

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> "24",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/business-bank-account"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }

    "return 303 with StartDate having a specific date when the company is not incorporated" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate(any(), any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))
      when(mockDateService.addWorkingDays(ArgumentMatchers.eq(today), anyInt())).thenReturn(today.plus(2, DAYS))
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)
      mockGetCurrentProfile((Some(currentNonincorpProfile)))

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> "24",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/business-bank-account"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }
  }
}
