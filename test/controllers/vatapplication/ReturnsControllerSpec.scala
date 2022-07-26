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
import forms.{AccountingPeriodForm, ReturnFrequencyForm}
import models.api.UkCompany
import models.api.vatapplication._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.MandatoryDateModel
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.vatapplication.{AccountingPeriodView, mandatory_start_date_incorp_view, return_frequency_view, start_date_incorp_view}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class ReturnsControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val dateBefore2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T13:59:59")
  val dateAfter2pm: LocalDateTime = LocalDateTime.parse("2018-03-19T14:00:00")
  val dateBefore2pmBH: LocalDateTime = LocalDateTime.parse("2018-03-28T13:59:59")
  val dateAfter2pmBH: LocalDateTime = LocalDateTime.parse("2018-03-28T14:00:00")

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile), currDate: LocalDateTime = dateBefore2pm, minDaysInFuture: Int = 3) {
    val view: mandatory_start_date_incorp_view = app.injector.instanceOf[mandatory_start_date_incorp_view]
    val returnFrequencyView: return_frequency_view = app.injector.instanceOf[return_frequency_view]
    val startDateIncorpView: start_date_incorp_view = app.injector.instanceOf[start_date_incorp_view]
    val accountingPeriodView: AccountingPeriodView = app.injector.instanceOf[AccountingPeriodView]
    val testController = new ReturnsController(
      mockSessionService,
      mockAuthClientConnector,
      movkVatApplicationService,
      mockApplicantDetailsServiceOld,
      mockTimeService,
      mockVatRegistrationService,
      view,
      returnFrequencyView,
      startDateIncorpView,
      accountingPeriodView
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
    mockAllTimeService(currDate, minDaysInFuture)
  }

  val emptyReturns: VatApplication = VatApplication()
  val voluntary = true

  "accountsPeriodPage" should {
    "return OK when returns are present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(JanuaryStagger))))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitAccountsPeriod" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.ReturnsController.submitAccountPeriods)

    "redirect to the Join Flat Rate page when they select the jan apr jul oct option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(JanuaryStagger))))
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.janStaggerKey
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/join-flat-rate")
      }
    }

    "redirect to the Join Flat Rate page when they select the feb may aug nov option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(FebruaryStagger))))
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.febStaggerKey
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/join-flat-rate")
      }
    }

    "redirect to the Join Flat Rate page when they select the mar may sep dec option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(MarchStagger))))
      when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(UkCompany))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> AccountingPeriodForm.marStaggerKey
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/join-flat-rate")
      }
    }

    "return 400 when they do not select an option" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> ""
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when they submit an invalid choice" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "returnsFrequencyPage" should {
    "return OK when returns are present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Monthly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are present with AAS" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Annual))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return Other when returns are present without AAS" in new Setup {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Quarterly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(false))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe SEE_OTHER
      }
    }
  }

  "submitReturnsFrequency" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.ReturnsController.submitReturnsFrequency)

    "redirect to the Join Flat Rate page when they select the monthly option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Monthly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> ReturnFrequencyForm.monthlyKey
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/join-flat-rate")
      }
    }

    "redirect to the account periods page when they select the quarterly option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Quarterly))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> ReturnFrequencyForm.quarterlyKey
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/submit-vat-returns")
      }
    }

    "redirect to the last month of accounting year page when they select the annual option" in new Setup {
      when(movkVatApplicationService.saveVatApplication(any())(any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(returnsFrequency = Some(Annual))))
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> ReturnFrequencyForm.annualKey
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/last-month-of-accounting-year")
      }
    }

    "return BAD_REQUEST when no option is selected" in new Setup {
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(true))
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> ""
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when an invalid option is submitted" in new Setup {
      when(movkVatApplicationService.isEligibleForAAS(any(), any()))
        .thenReturn(Future.successful(false))
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "INVALID_SELECTION"
      )

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }

  "mandatoryStartPage" should {
    "show the page" when {
      "return OK when not voluntary" in new Setup {
        when(movkVatApplicationService.retrieveMandatoryDates(any(), any()))
          .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        callAuthorised(testController.mandatoryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitMandatoryStart" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.ReturnsController.submitMandatoryStart)

    "redirect to the accounts period page if the calculated date is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> DateSelection.calculated_date
      )
      val calculatedDate: LocalDate = LocalDate.now().minusMonths(3)

      when(movkVatApplicationService.saveVatApplication(ArgumentMatchers.eq(calculatedDate))(any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
        .thenReturn(Future.successful(calculatedDate))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the accounts period page if chosen date is before the calculated date and after the earliest of incorpDate and 4 years ago" in new Setup {
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
        .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "provide a bad request" when {

      "user submit a past start date earlier than 4 years ago" in new Setup {
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
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(3))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        submitAuthorised(testController.submitMandatoryStart, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }

      "user submit a past start date earlier than 4 incorp date" in new Setup {
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
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(5))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        submitAuthorised(testController.submitMandatoryStart, request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "user submit a date older than the incorp date but within 4 years" in new Setup {
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
          .thenReturn(Future.successful(Some(LocalDate.now.minusYears(2))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

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

        when(movkVatApplicationService.getVatApplication(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        callAuthorised(testController.voluntaryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  "submitVoluntaryStartPage" should {
    val fakeRequest = FakeRequest(controllers.vatapplication.routes.ReturnsController.submitVoluntaryStart)

    "redirect to the returns frequency page if company registration date is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> DateSelection.company_registration_date
      )

      when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatApplication))

      when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the returns frequency page" when {

      "user submit a valid start date if the submission occurred before 2pm" in new Setup(currDate = dateBefore2pm) {

        val nowPlusFive: LocalDate = dateBefore2pm.toLocalDate.plusDays(3)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        submitAuthorised(testController.submitVoluntaryStart, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
        }
      }

      "user submit a valid start date if the submission occurred after 2pm" in new Setup(currDate = dateAfter2pm, minDaysInFuture = 4) {

        val nowPlusFive: LocalDate = dateAfter2pm.toLocalDate.plusDays(4)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> DateSelection.specific_date,
          "startDate.month" -> nowPlusFive.getMonthValue.toString,
          "startDate.year" -> nowPlusFive.getYear.toString,
          "startDate.day" -> nowPlusFive.getDayOfMonth.toString
        )

        when(movkVatApplicationService.saveVoluntaryStartDate(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(validVatApplication))

        when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
          .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

        when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        submitAuthorised(testController.submitVoluntaryStart, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)

        }
      }

      "provide a bad request" when {

        "user submit a past start date earlier than 4 years ago" in new Setup {
          val nowMinusFive: LocalDate = dateBefore2pm.toLocalDate.minusYears(5)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2016, 1, 1))))

          when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
            .thenReturn(Future.successful(validEligibilitySubmissionData))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a past start date earlier than date of incorporation when it's older than 4 years ago" in new Setup {
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2011, 1, 1))))

          when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
            .thenReturn(Future.successful(validEligibilitySubmissionData))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a date older than the incorp date but within 4 years" in new Setup {
          val requestedDate: LocalDate = LocalDate.of(2010, 1, 1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "value" -> DateSelection.specific_date,
            "startDate.month" -> requestedDate.getMonthValue.toString,
            "startDate.year" -> requestedDate.getYear.toString,
            "startDate.day" -> requestedDate.getDayOfMonth.toString
          )

          when(mockApplicantDetailsServiceOld.getDateOfIncorporation(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2011, 1, 1))))

          when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
            .thenReturn(Future.successful(validEligibilitySubmissionData))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }
      }
    }
  }
}