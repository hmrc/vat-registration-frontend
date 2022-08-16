/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.vatapplication

import _root_.models._
import fixtures.VatRegistrationFixture
import models.api.vatapplication._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.MandatoryDateModel
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.vatapplication.mandatory_start_date_incorp_view

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class MandatoryStartDateControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val dateBefore2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T13:59:59")
  val dateAfter2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T14:00:00")
  val dateBefore2pmBH: LocalDateTime = LocalDateTime.parse("2018-03-28T13:59:59")
  val dateAfter2pmBH: LocalDateTime = LocalDateTime.parse("2018-03-28T14:00:00")

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile), currDate: LocalDateTime = dateBefore2pm, minDaysInFuture: Int = 3) {
    val view: mandatory_start_date_incorp_view = app.injector.instanceOf[mandatory_start_date_incorp_view]
    val testController = new MandatoryStartDateController(
      mockSessionService, mockAuthClientConnector, movkVatApplicationService, view
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
    mockAllTimeService(currDate, minDaysInFuture)
  }

  val emptyReturns: VatApplication = VatApplication()
  val voluntary = true

  "mandatoryStartPage" should {
    "show the page" when {
      "return OK when not voluntary" in new Setup {
        val incorpDate: LocalDate = LocalDate.of(2017, 1, 1)

        when(movkVatApplicationService.retrieveMandatoryDates(any(), any()))
          .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        callAuthorised(testController.show) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitMandatoryStart" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.MandatoryStartDateController.submit)

    "redirect to the accounts period page if the calculated date is selected" in new Setup {
      val incorpDate: LocalDate = LocalDate.now.minusYears(3)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> DateSelection.calculated_date
      )
      val calculatedDate: LocalDate = LocalDate.now().minusMonths(3)

      when(movkVatApplicationService.saveVatApplication(ArgumentMatchers.eq(calculatedDate))(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
        .thenReturn(Future.successful(calculatedDate))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(incorpDate)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
        .thenReturn(Future.successful(incorpDate))

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url)
      }
    }

    "redirect to the accounts period page if chosen date is before the calculated date and after the earliest of incorpDate and 4 years ago" in new Setup {
      val incorpDate: LocalDate = LocalDate.now.minusYears(3)
      val specificDate: LocalDate = LocalDate.now.minusYears(1)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> DateSelection.specific_date,
        "date.month" -> specificDate.getMonthValue.toString,
        "date.year" -> specificDate.getYear.toString,
        "date.day" -> specificDate.getDayOfMonth.toString
      )

      when(movkVatApplicationService.saveVatApplication(ArgumentMatchers.eq(specificDate))(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
        .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(incorpDate)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
        .thenReturn(Future.successful(incorpDate))

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url)
      }
    }

    "provide a bad request" when {

      "user submit a past start date earlier than 4 years ago" in new Setup {
        val incorpDate: LocalDate = LocalDate.now.minusYears(3)
        val specificDate: LocalDate = LocalDate.now.minusYears(5)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "date.month" -> specificDate.getMonthValue.toString,
          "date.year" -> specificDate.getYear.toString,
          "date.day" -> specificDate.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }

      "user submit a past start date earlier than 4 incorp date" in new Setup {
        val incorpDate: LocalDate = LocalDate.now.minusYears(5)
        val specificDate: LocalDate = LocalDate.now.minusYears(6)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "date.month" -> specificDate.getMonthValue.toString,
          "date.year" -> specificDate.getYear.toString,
          "date.day" -> specificDate.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "user submit a date older than the incorp date but within 4 years" in new Setup {
        val incorpDate: LocalDate = LocalDate.now.minusYears(2)
        val specificDate: LocalDate = LocalDate.now.minusYears(3)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "date.month" -> specificDate.getMonthValue.toString,
          "date.year" -> specificDate.getYear.toString,
          "date.day" -> specificDate.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}
