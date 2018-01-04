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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.api.{VatEligibilityChoice, VatExpectedThresholdPostIncorp, VatServiceEligibility, VatThresholdPostIncorp}
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{verify, when}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class MandatoryStartDateControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object MandatoryStartDateController extends MandatoryStartDateController(
    ds,
    mockAuthConnector,
    mockKeystoreConnector,
    mockVatRegistrationService,
    mockS4LService
  )

  val expectedThresholdDate = LocalDate.of(2017, 6, 21)
  val expectedThreshold = Some(VatExpectedThresholdPostIncorp(expectedOverThresholdSelection = true, Some(expectedThresholdDate)))

  val knownThresholdDate = LocalDate.of(2017, 4, 25)
  val knownThreshold = Some(VatThresholdPostIncorp(overThresholdSelection = true, Some(knownThresholdDate)))

  val knownThresholdDateNovember = LocalDate.of(2017, 11, 25)
  val knownThresholdNov = Some(VatThresholdPostIncorp(overThresholdSelection = true, Some(knownThresholdDateNovember)))

  val mandatoryEligibilityThreshold: Option[VatServiceEligibility] = Some(
      validServiceEligibility(
        VatEligibilityChoice.NECESSITY_OBLIGATORY,
        None,
        expectedThreshold
      )
    )

  val now = LocalDate.now()
  val exceedThreeMonth = now.plusMonths(3).plusDays(1)
  val fourYearsAgo = now.minusYears(4).minusDays(1)
  val dayAfterExpected = expectedThresholdDate.plusDays(1)
  val dayBeforeExpected = expectedThresholdDate.minusDays(1)

  val validPastRegIncorpDate : LocalDate = LocalDate.of(2017, 2, 21)

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.MandatoryStartDateController.show()}" should {
    "display the mandatory start date confirmation page to the user if they are not incorporated" in {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentNonincorpProfile)))

      callAuthorised(MandatoryStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }

    "error if they are incorporated with nothing in S4L with no vat scheme" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))
      mockGetCurrentProfile()
      intercept[RuntimeException] {
        callAuthorised(MandatoryStartDateController.show) { result =>
          status(result) mustBe OK
        }
      }
    }

    "display the mandatory start date incorp page to the user if they are incorporated with nothing in S4L with a vat scheme" in {
      save4laterReturnsNoViewModel[StartDateView]()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))
      mockGetCurrentProfile()
      callAuthorised(MandatoryStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }

    "display the mandatory start date incorp page to the user if they are incorporated with data in S4L" in {
      val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

      save4laterReturnsViewModel(startDate)()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      mockGetCurrentProfile()

      callAuthorised(MandatoryStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }
  }

  s"POST ${routes.MandatoryStartDateController.submit()}" should {
    "redirect the user to the bank account page after clicking continue on the mandatory start date confirmation page if they are not incorped" in {


      mockGetCurrentProfile(Some(currentNonincorpProfile))

      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)
      callAuthorised(MandatoryStartDateController.submit) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView().url)
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }

    "be a bad request if they're incorped and the form has no data" in {
      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody()) {
        result =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "be a bad request if they're incorped and the form has partial data" in {
      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> "31",
        "startDate.month" -> "",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "be a bad request if they're incorped and the form date is 3 months in the future" in {
      mockGetCurrentProfile()
      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> exceedThreeMonth.getDayOfMonth.toString,
        "startDate.month" -> exceedThreeMonth.getMonthValue.toString,
        "startDate.year" -> exceedThreeMonth.getYear.toString
      )) {
        result =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "be a bad request if they're incorped and the form date is 4 years in the past" in {
      mockGetCurrentProfile()
      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> fourYearsAgo.getDayOfMonth.toString,
        "startDate.month" -> fourYearsAgo.getMonthValue.toString,
        "startDate.year" -> fourYearsAgo.getYear.toString
      )) {
        result =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "be a bad request if they're incorped and the form date is after the calculated date" in {
      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> dayAfterExpected.getDayOfMonth.toString,
        "startDate.month" -> dayAfterExpected.getMonthValue.toString,
        "startDate.year" -> dayAfterExpected.getYear.toString
      )) {
        result =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 with valid earlier date data" in {
      save4laterExpectsSave[StartDateView]()
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)

      mockGetCurrentProfile(Some(
          currentProfile().copy(incorporationDate = Some(validPastRegIncorpDate))))

      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> dayBeforeExpected.getDayOfMonth.toString,
        "startDate.month" -> dayBeforeExpected.getMonthValue.toString,
        "startDate.year" -> dayBeforeExpected.getYear.toString
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result) mustBe Some(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView().url)
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }

    "return 303 with the calculated latest date selected" in {
      save4laterExpectsSave[StartDateView]()
      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)

      mockGetCurrentProfile(Some(
          currentProfile().copy(incorporationDate = Some(validPastRegIncorpDate))))
      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      submitAuthorised(MandatoryStartDateController.submit, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.COMPANY_REGISTRATION_DATE,
        "startDate.day" -> "",
        "startDate.month" -> "",
        "startDate.year" -> ""
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result) mustBe Some(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView().url)
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }
  }

  "mandatoryStartDate" should {
    "return the calculated start date provided the vat scheme is there" in {
      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme.copy(vatServiceEligibility = mandatoryEligibilityThreshold)))

      mockGetCurrentProfile()
      await(MandatoryStartDateController.mandatoryStartDate(ArgumentMatchers.any[CurrentProfile], ArgumentMatchers.any[HeaderCarrier])) mustBe Some(expectedThresholdDate)
    }

    "return the calculated start date provided the vat scheme isn't there" in {
      when(mockVatRegistrationService.getVatScheme(any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      mockGetCurrentProfile()
      await(MandatoryStartDateController.mandatoryStartDate(ArgumentMatchers.any[CurrentProfile], ArgumentMatchers.any[HeaderCarrier])) mustBe None
    }
  }


  "calculateMandatoryStartDate" should {
    "return the calculated start date if only an expected threshold is provided" in {
      MandatoryStartDateController.calculateMandatoryStartDate(None, expectedThreshold) mustBe Some(expectedThresholdDate)
    }

    "return the calculated start date if only an known threshold is provided" in {
      MandatoryStartDateController.calculateMandatoryStartDate(knownThreshold, None) mustBe
        Some(knownThresholdDate.withDayOfMonth(1).plusMonths(2))
    }

    "return no calculated start date is no thresholds were provided" in {
      MandatoryStartDateController.calculateMandatoryStartDate(None, None) mustBe None
    }

    "return the calculated start date if an known threshold is earlier than expected" in {
      MandatoryStartDateController.calculateMandatoryStartDate(knownThreshold, expectedThreshold) mustBe
        Some(knownThresholdDate.withDayOfMonth(1).plusMonths(2))
    }

    "return the calculated start date if an known threshold being in November" in {

      MandatoryStartDateController.calculateMandatoryStartDate(knownThresholdNov, None) mustBe
        Some(knownThresholdDateNovember.withDayOfMonth(1).plusMonths(2))
    }
  }
}
