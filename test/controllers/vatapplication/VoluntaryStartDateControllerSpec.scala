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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.vatapplication.start_date_incorp_view

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class VoluntaryStartDateControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val dateBefore2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T13:59:59")
  val dateAfter2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T14:00:00")

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile), currDate: LocalDateTime = dateBefore2pm, minDaysInFuture: Int = 3) {
    val testController = new VoluntaryStartDateController(
      mockSessionService,
      mockAuthClientConnector,
      movkVatApplicationService,
      mockTimeService,
      app.injector.instanceOf[start_date_incorp_view]
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
    mockAllTimeService(currDate, minDaysInFuture)
  }

  val emptyReturns: VatApplication = VatApplication()
  val voluntary = true

  "voluntaryStartPage" should {
    "show the page" when {
      "return an OK when returns are not present" in new Setup {
        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(testIncorpDate)))

        when(movkVatApplicationService.getVatApplication(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(testIncorpDate))

        callAuthorised(testController.show) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitVoluntaryStartPage" should {
    val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.VoluntaryStartDateController.submit)

    "redirect to the returns frequency page if company registration date is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> DateSelection.company_registration_date
      )

      when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatApplication))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(incorpDate)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
        .thenReturn(Future.successful(incorpDate))

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)
      }
    }

    "redirect to the returns frequency page" when {

      "user submit a valid start date if the submission occurred before 2pm" in new Setup(currDate = dateBefore2pm) {
        val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
        val nowPlusFive: LocalDate = dateBefore2pm.toLocalDate.plusDays(3)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)
        }
      }

      "user submit a valid start date if the submission occurred after 2pm" in new Setup(currDate = dateAfter2pm, minDaysInFuture = 4) {
        val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
        val nowPlusFive: LocalDate = dateAfter2pm.toLocalDate.plusDays(4)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(incorpDate)))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        when(movkVatApplicationService.calculateEarliestStartDate()(any(), any()))
          .thenReturn(Future.successful(incorpDate))

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)

        }
      }

      "provide a bad request" when {

        "user submit a past start date earlier than 4 years ago" in new Setup {
          val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
          val nowMinusFive: LocalDate = dateBefore2pm.toLocalDate.minusYears(5)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

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

        "user submit a past start date earlier than date of incorporation when it's older than 4 years ago" in new Setup {
          val incorpDate: LocalDate = LocalDate.of(2011, 1, 1)
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

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
          val incorpDate: LocalDate = LocalDate.of(2011, 1, 1)
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

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
}
