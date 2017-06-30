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

package controllers.frs


import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import common.Now
import fixtures.VatRegistrationFixture
import forms.frs.FrsStartDateFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.VatFlatRateScheme
import models.view.frs.FrsStartDateView
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class FrsStartDateControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val today: LocalDate = LocalDate.of(2017, 3, 21)
  val frsStartDateFormFactory = new FrsStartDateFormFactory(mockDateService, Now[LocalDate](today))
  implicit val localDateOrdering = frsStartDateFormFactory.LocalDateOrdering

  object FrsTestStartDateController extends FrsStartDateController(frsStartDateFormFactory, ds)(mockS4LService, mockVatRegistrationService) {
    implicit val fixedToday = Now[LocalDate](today)
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.FrsStartDateController.show())

  s"GET ${routes.FrsStartDateController.show()}" should {

    "return HTML when there's a frs start date in S4L" in {
      val frsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(LocalDate.now))

      save4laterReturnsViewModel(frsStartDate)()

      callAuthorised(FrsTestStartDateController.show) {
        _ includesText ("When do you want to join the Flat Rate Scheme?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[FrsStartDateView]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(FrsTestStartDateController.show) {
        _ includesText ("When do you want to join the Flat Rate Scheme?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[FrsStartDateView]()

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(FrsTestStartDateController.show) {
        _ includesText ("When do you want to join the Flat Rate Scheme?")
      }
    }
  }

  s"POST ${routes.FrsStartDateController.submit()}" should {

    "return 400 when no data posted" in {
      save4laterReturnsViewModel(StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.now)))()

      submitAuthorised(
        FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {
      save4laterReturnsViewModel(StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.now)))()

      submitAuthorised(
        FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
          "frsStartDate.day" -> "1",
          "frsStartDate.month" -> "",
          "frsStartDate.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 with Different Date selected and date that is less than 2 working days in the future" in {
      save4laterExpectsSave[FrsStartDateView]()
      save4laterReturnsViewModel(StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.now)))()

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> "20",
        "frsStartDate.month" -> "3",
        "frsStartDateDate.year" -> "2017"
      )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with VAT Registration Date selected" in {
      save4laterExpectsSave[FrsStartDateView]()
      save4laterReturnsViewModel(StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.now)))()

      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(VatFlatRateScheme(true).pure)

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE
      )) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }
    }

    "return 303 with Different Date entered" in {

      val minDate: LocalDate = today.plusDays(30)
      save4laterExpectsSave[FrsStartDateView]()
      save4laterReturnsViewModel(FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(LocalDate.now)))()

      when(mockDateService.addWorkingDays(Matchers.eq(today), anyInt())).thenReturn(today.plus(2, DAYS))
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(VatFlatRateScheme(true).pure)

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> minDate.getDayOfMonth.toString,
        "frsStartDate.month" -> minDate.getMonthValue.toString,
        "frsStartDate.year" -> minDate.getYear.toString

      )) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }

    }

    "return 303 with Vat Registration Date selected" in {
      val minDate: LocalDate = today.plusDays(30)
      save4laterReturnsViewModel(StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.now)))()
      save4laterExpectsSave[FrsStartDateView]()
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(VatFlatRateScheme(true).pure)

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE

      )) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }

    }

    "return 303 with Vat Choice Start Date is Null " in {
      save4laterExpectsSave[FrsStartDateView]()
      save4laterReturnsNoViewModel[StartDateView]()
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any())).thenReturn(VatFlatRateScheme(true).pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE

      )) {
        _ redirectsTo s"$contextRoot/check-your-answers"
      }

    }

  }
}
