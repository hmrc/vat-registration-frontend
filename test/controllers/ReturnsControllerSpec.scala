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
import services.VoluntaryPageViewModel
import testHelpers.{ControllerSpec, MockMessages}

import scala.concurrent.Future

class ReturnsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages with TimeServiceMock {
  val dateBefore2pm = LocalDateTime.parse("2018-03-19T13:59:59")
  val dateAfter2pm = LocalDateTime.parse("2018-03-19T14:00:00")
  val dateBefore2pmBH = LocalDateTime.parse("2018-03-28T13:59:59")
  val dateAfter2pmBH = LocalDateTime.parse("2018-03-28T14:00:00")

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile), currDate: LocalDateTime = dateBefore2pm, minDaysInFuture: Int = 3) {
    val testController = new ReturnsController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockReturnsService,
      mockMessagesAPI,
      mockTimeService
    )

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(cp)
    mockAllTimeService(currDate, minDaysInFuture)
  }

  val emptyReturns: Returns = Returns.empty
  val voluntary = true

  "chargeExpectancyPage" should {
    "return OK when returns are found" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))

      callAuthorised(testController.chargeExpectancyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not found" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      callAuthorised(testController.chargeExpectancyPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitChargeExpectancy" should {
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitChargeExpectancy())

    "return SEE_OTHER when they expect to reclaim more vat than they charge and redirect to VAT Start Page - mandatory flow" in new Setup {
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))

      when(mockReturnsService.getThreshold()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "true"
      )

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "return SEE_OTHER when they don't expect to reclaim more vat than they charge and redirect to VAT Start Page - voluntarily flow" in new Setup {
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(false))))

      when(mockReturnsService.getThreshold()(any(), any(), any()))
        .thenReturn(Future.successful(voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "false"
      )

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> ""
      )

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST if the option selected is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "INVALID-OPTION"
      )

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

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

        callAuthorised(testController.mandatoryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }

    "redirect to a different page" when {
      "redirect to the voluntary vat start page if they are on a voluntary journey" in new Setup {
        when(mockReturnsService.getThreshold()(any(), any(), any()))
          .thenReturn(Future.successful(voluntary))

        callAuthorised(testController.mandatoryStartPage) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
        }
      }
    }
  }

  "submitMandatoryStart" should {
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitMandatoryStart())

    "redirect to the accounts period page" in new Setup() {
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.accountPeriodsPage().url)
      }
    }
  }

  "voluntaryStartPage" should {
    val dateSelection = (DateSelection.calculated_date, Some(LocalDate.now()))
    val calcDate = LocalDate.now()
    val voluntaryViewModel = VoluntaryPageViewModel(Some(dateSelection), Some(calcDate))
    "show the page" when {
      "return an OK when returns are not present" in new Setup {
        when(mockReturnsService.getThreshold()(any(), any(), any()))
          .thenReturn(Future.successful(voluntary))

        when(mockReturnsService.voluntaryStartPageViewModel()(any(), any(), any()))
          .thenReturn(Future.successful(voluntaryViewModel))

        callAuthorised(testController.voluntaryStartPage) { result =>
          status(result) mustBe OK
        }
      }
    }

    "redirect to a different page" when {
      "redirect to the mandatory vat start page if they are on a mandatory journey" in new Setup {
        when(mockReturnsService.getThreshold()(any(), any(), any()))
          .thenReturn(Future.successful(!voluntary))

        callAuthorised(testController.voluntaryStartPage) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
        }
      }
    }
  }

  "submitVoluntaryStartPage" should {

    val date = LocalDate.of(2017, 1, 1)
    val returns = Returns(Some(true), Some(Frequency.quarterly), Some(Stagger.jan), Some(Start(Some(date))))
    val fakeRequest = FakeRequest(controllers.routes.ReturnsController.submitVoluntaryStart())

    "redirect to the returns frequency page if they select the date of incorp whilst in pre incorp state" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.company_registration_date
      )

      when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

      when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(returns))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.returnsFrequencyPage().url)
      }
    }

    "redirect to the returns frequency page (whilst in pre incorp state)" when {
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

        submitAuthorised(testController.submitVoluntaryStart, request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.returnsFrequencyPage().url)

        }
      }

      "provide a bad request (whilst in pre incorp state)" when {
        "user submit a past start date" in new Setup {
          val nowMinusFive: LocalDate = dateBefore2pm.toLocalDate.minusDays(5)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a future start date not more than 2 working days if the submission occurred before 2pm" in new Setup {
          val nowMinusFive: LocalDate = dateBefore2pm.toLocalDate.plusDays(2)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a future start date not more than 2 working days (except bank holidays) if the submission occurred before 2pm" in new Setup(currDate = dateBefore2pmBH) {
          val nowMinusFive: LocalDate = dateBefore2pmBH.toLocalDate.minusDays(6)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a future start date not more than 3 working days if the submission occurred after 2pm" in new Setup(currDate = dateAfter2pm) {
          val nowMinusFive: LocalDate = dateAfter2pm.toLocalDate.plusDays(1)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }

        "user submit a future start date not more than 3 working days (except bank holidays) if the submission occurred after 2pm" in new Setup(currDate = dateAfter2pmBH) {
          val nowMinusFive: LocalDate = dateAfter2pmBH.toLocalDate.plusDays(2)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
            "startDateRadio" -> DateSelection.specific_date,
            "startDate.month" -> nowMinusFive.getMonthValue.toString,
            "startDate.year" -> nowMinusFive.getYear.toString,
            "startDate.day" -> nowMinusFive.getDayOfMonth.toString
          )

          when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

          submitAuthorised(testController.submitVoluntaryStart, request) { result =>
            status(result) mustBe BAD_REQUEST
          }
        }
      }
    }
  }
}
