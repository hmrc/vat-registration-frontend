/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import java.time.{LocalDate, LocalDateTime}

import _root_.models._
import fixtures.VatRegistrationFixture
import mocks.TimeServiceMock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.MandatoryDateModel
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class ReturnsControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val dateBefore2pm = LocalDateTime.parse("2018-03-19T13:59:59")
  val dateAfter2pm = LocalDateTime.parse("2018-03-19T14:00:00")
  val dateBefore2pmBH = LocalDateTime.parse("2018-03-28T13:59:59")
  val dateAfter2pmBH = LocalDateTime.parse("2018-03-28T14:00:00")

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile), currDate: LocalDateTime = dateBefore2pm, minDaysInFuture: Int = 3) {
    val testController = new ReturnsController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockReturnsService,
      mockApplicantDetailsServiceOld,
      mockTimeService
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
    mockAllTimeService(currDate, minDaysInFuture)
  }

  val emptyReturns: Returns = Returns.empty
  val voluntary = true

  "accountsPeriodPage" should {
    "return OK when returns are present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitAccountsPeriod" should {
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitAccountPeriods())

    "redirect to the bank account date page when they select the jan apr jul oct option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.jan
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/companys-bank-account")
      }
    }

    "redirect to the bank account page when they select the jan apr jul oct option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.jan
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/companys-bank-account")
      }
    }

    "redirect to the bank account page when they select the feb may aug nov option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.feb))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.feb
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/companys-bank-account")
      }
    }

    "redirect to the bank account page when they select the mar may sep dec option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.mar))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.mar
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/companys-bank-account")
      }
    }

    "return 400 when they do not select an option" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> ""
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when they submit an invalid choice" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "returnsFrequencyPage" should {
    "return OK when returns are present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.monthly))))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitReturnsFrequency" should {
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitReturnsFrequency())

    "redirect to the bank account page when they select the monthly option" in new Setup {
      when(mockReturnsService.saveFrequency(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.monthly))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> Frequency.monthly
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/companys-bank-account")
      }
    }

    "redirect to the account periods page when they select the quarterly option" in new Setup {
      when(mockReturnsService.saveFrequency(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.quarterly))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> Frequency.quarterly
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/submit-vat-returns")
      }
    }

    "return BAD_REQUEST when no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> ""
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when an invalid option is submitted" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "requestFrequencyRadio" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }

  "mandatoryStartPage" should {
    "show the page" when {
      "return OK when not voluntary" in new Setup {
        when(mockReturnsService.getThreshold()(any(), any(), any()))
          .thenReturn(Future.successful(!voluntary))

        when(mockReturnsService.retrieveMandatoryDates(any(), any(), any()))
          .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

        callAuthorised(testController.mandatoryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitMandatoryStart" should {
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitMandatoryStart())

    "redirect to the accounts period page if the calculated date is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.calculated_date
      )

      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
        .thenReturn(Future.successful(LocalDate.now().minusMonths(3)))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.accountPeriodsPage().url)
      }
    }

    "redirect to the accounts period page if chosen date is before the calculated date and after the eaeliest of incorpDate and 4 years ago" in new Setup {
      val specificDate: LocalDate = LocalDate.now.minusYears(1)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.specific_date,
        "startDate.month" -> specificDate.getMonthValue.toString,
        "startDate.year" -> specificDate.getYear.toString,
        "startDate.day" -> specificDate.getDayOfMonth.toString
      )

      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
        .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.accountPeriodsPage().url)
      }
    }

    "provide a bad request" when {

      "user submit a past start date earlier than 4 years ago" in new Setup {
        val specificDate: LocalDate = LocalDate.now.minusYears(5)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> DateSelection.specific_date,
          "startDate.month" -> specificDate.getMonthValue.toString,
          "startDate.year" -> specificDate.getYear.toString,
          "startDate.day" -> specificDate.getDayOfMonth.toString
        )

        when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

        submitAuthorised(testController.submitMandatoryStart, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }

      "user submit a past start date earlier than 4 incorp date" in new Setup {
        val specificDate: LocalDate = LocalDate.now.minusYears(6)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> DateSelection.specific_date,
          "startDate.month" -> specificDate.getMonthValue.toString,
          "startDate.year" -> specificDate.getYear.toString,
          "startDate.day" -> specificDate.getDayOfMonth.toString
        )

        when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(5))))

        submitAuthorised(testController.submitMandatoryStart, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "user submit a date older than the incorp date but within 4 years" in new Setup {
        val specificDate: LocalDate = LocalDate.now.minusYears(3)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> DateSelection.specific_date,
          "startDate.month" -> specificDate.getMonthValue.toString,
          "startDate.year" -> specificDate.getYear.toString,
          "startDate.day" -> specificDate.getDayOfMonth.toString
        )

        when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyReturns))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
          .thenReturn(Future.successful(LocalDate.now.minusMonths(3)))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(2))))

        submitAuthorised(testController.submitMandatoryStart, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

  "voluntaryStartPage" should {
    "show the page" when {
      "return an OK when returns are not present" in new Setup {
        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(testIncorpDate)))

        when(mockReturnsService.getReturns(any(), any(), any()))
          .thenReturn(Future.successful(returns))

        callAuthorised(testController.voluntaryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitVoluntaryStartPage" should {

    val date = LocalDate.of(2017, 1, 1)
    val returns = Returns(Some(10000.5), Some(true), Some(Frequency.quarterly), Some(Stagger.jan), Some(Start(Some(date))))
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitVoluntaryStart())

    "redirect to the returns frequency page if company registration date is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.company_registration_date
      )

      when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

      when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(returns))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.returnsFrequencyPage().url)
      }
    }

    "redirect to the returns frequency page" when {

      "user submit a valid start date if the submission occurred before 2pm" in new Setup(currDate = dateBefore2pm) {
        when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

        val nowPlusFive: LocalDate = dateBefore2pm.toLocalDate.plusDays(3)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(returns))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

        submitAuthorised(testController.submitVoluntaryStart, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.returnsFrequencyPage().url)
        }
      }

      "user submit a valid start date if the submission occurred after 2pm" in new Setup(currDate = dateAfter2pm, minDaysInFuture = 4) {
        when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

        val nowPlusFive: LocalDate = dateAfter2pm.toLocalDate.plusDays(4)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(returns))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

        submitAuthorised(testController.submitVoluntaryStart, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.returnsFrequencyPage().url)

        }
      }

      "provide a bad request" when {

        "user submit a past start date earlier than 4 years ago" in new Setup {
          val nowMinusFive: LocalDate = dateBefore2pm.toLocalDate.minusYears(5)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a past start date earlier than date of incorporation when it's older than 4 years ago" in new Setup {
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2011, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a date older than the incorp date but within 4 years" in new Setup {
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2012, 1, 1))))

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2011, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }
      }
    }
  }
}
