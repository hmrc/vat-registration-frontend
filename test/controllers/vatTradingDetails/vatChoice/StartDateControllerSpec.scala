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
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class StartDateControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val today: LocalDate = LocalDate.of(2017, 3, 21)

  val startDateFormFactory = new StartDateFormFactory(mockDateService, Now[LocalDate](today))

  object TestStartDateController extends StartDateController(startDateFormFactory, mockPPService, ds)(mockS4LService, mockVatRegistrationService) {
    implicit val fixedToday = Now[LocalDate](today)
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.StartDateController.show()}" should {

    "return HTML when there's a start date in S4L" in {
      val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

      save4laterReturnsViewModel(startDate)()
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      callAuthorised(TestStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

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
      submitAuthorised(
        TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {
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

    "return 303 with valid data" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.COMPANY_REGISTRATION_DATE,
        "startDate.day" -> "21",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/trading-name"
      }
    }

    "return 303 with valid data BUSINESS_START_DATE" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.BUSINESS_START_DATE
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/trading-name"
      }
    }

    "return 303 with StartDate having a specific date" in {
      save4laterExpectsSave[StartDateView]()
      when(mockPPService.getCTActiveDate()(any())).thenReturn(OptionT.some(LocalDate.of(2017, 4, 20)))
      when(mockDateService.addWorkingDays(Matchers.eq(today), anyInt())).thenReturn(today.plus(2, DAYS))

      submitAuthorised(TestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> "24",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/trading-name"
      }
    }
  }
}
