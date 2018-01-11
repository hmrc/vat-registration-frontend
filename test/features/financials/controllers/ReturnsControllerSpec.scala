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

package features.financials.controllers

import java.time.LocalDate

import cats.data.OptionT
import connectors.KeystoreConnect
import features.returns._
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future


class ReturnsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {
  val mockReturnsService: ReturnsServiceImpl = mock[ReturnsServiceImpl]

  trait Setup {
    val testController = new ReturnsController {
      override val authConnector: AuthConnector = mockAuthConnector
      override val returnsService: ReturnsService = mockReturnsService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override def messagesApi: MessagesApi = mockMessagesAPI
    }
    mockAllMessages
  }

  val emptyReturns: Returns = Returns.empty
  val voluntary = true

  val now = LocalDate.now()
  val startAtIncorp = Start(Some(currentProfile.incorporationDate.get))
  val startAtIncorpMinusTwo = Start(Some(currentProfile.incorporationDate.get.minusDays(2)))

  "chargeExpectancyPage" should {
    "return OK when returns are found" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.chargeExpectancyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not found" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.chargeExpectancyPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitChargeExpectancy" should {
    val fakeRequest = FakeRequest(features.returns.routes.ReturnsController.submitChargeExpectancy())

    "return SEE_OTHER when they expect to reclaim more vat than they charge" in new Setup {
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(true))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "true"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/how-often-do-you-want-to-submit-vat-returns")
      }
    }

    "return SEE_OTHER when they don't expect to reclaim more vat than they charge" in new Setup {
      when(mockReturnsService.saveReclaimVATOnMostReturns(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reclaimVatOnMostReturns = Some(false))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "false"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-return-periods-end")
      }
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> ""
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST if the option selected is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "chargeExpectancyRadio" -> "INVALID-OPTION"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitChargeExpectancy, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "accountsPeriodPage" should {
    "return OK when returns are present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.accountPeriodsPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitAccountsPeriod" should {
    val fakeRequest = FakeRequest(features.returns.routes.ReturnsController.submitAccountPeriods())

    "redirect to the mandatory date page when they select the jan apr jul oct option when on a mandatory flow" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.jan
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "redirect to the voluntary date page when they select the jan apr jul oct option when on a voluntary flow" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.jan))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.jan
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/what-do-you-want-your-vat-start-date-to-be")
      }
    }

    "return SEE_OTHER when they select the feb may aug nov option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(),any(),any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.feb))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.feb
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "return SEE_OTHER when they select the mar may sep dec option" in new Setup {
      when(mockReturnsService.saveStaggerStart(any())(any(),any(),any()))
        .thenReturn(Future.successful(emptyReturns.copy(staggerStart = Some(Stagger.mar))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> Stagger.mar
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "return 400 when they do not select an option" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> ""
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when they submit an invalid choice" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "accountingPeriodRadio" -> "INVALID_SELECTION"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitAccountPeriods, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "returnsFrequencyPage" should {
    "return OK when returns are present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.monthly))))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.returnsFrequencyPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitReturnsFrequency" should {
    val fakeRequest = FakeRequest(features.returns.routes.ReturnsController.submitReturnsFrequency())

    "redirect to the mandatory date page when they select the monthly option on the mandatory flow" in new Setup {
      when(mockReturnsService.saveFrequency(any())(any(),any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.monthly))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> Frequency.monthly
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-start-date")
      }
    }

    "redirect to the voluntary date page when they select the monthly option on the voluntary flow" in new Setup {
      when(mockReturnsService.saveFrequency(any())(any(),any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.monthly))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> Frequency.monthly
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/what-do-you-want-your-vat-start-date-to-be")
      }
    }

    "redirect to the account periods page when they select the quarterly option" in new Setup {
      when(mockReturnsService.saveFrequency(any())(any(),any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(frequency = Some(Frequency.quarterly))))

      when(mockReturnsService.getEligibilityChoice()(any(), any(), any()))
        .thenReturn(Future.successful(!voluntary))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> Frequency.quarterly
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/vat-return-periods-end")
      }
    }

    "return BAD_REQUEST when no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "returnFrequencyRadio" -> ""
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when an invalid option is submitted" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "requestFrequencyRadio" -> "INVALID_SELECTION"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitReturnsFrequency, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }

  "mandatoryStartPage" should {
    val incorpDate = currentProfile.incorporationDate.get
    val testDate = LocalDate.of(2018, 1, 1)

    "return OK when the company is incorporated, returns are present and calc date equals stored date" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorp))))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockReturnsService.retrieveMandatoryDates(any(), any(), any()))
        .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))

      callAuthorised(testController.mandatoryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when the company is not incorporated" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorp))))

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      when(mockReturnsService.retrieveMandatoryDates(any(), any(), any()))
        .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))

      callAuthorised(testController.mandatoryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return an OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockReturnsService.retrieveMandatoryDates(any(), any(), any()))
        .thenReturn(Future.successful(MandatoryDateModel(testDate, Some(testDate), Some(DateSelection.calculated_date))))


      callAuthorised(testController.mandatoryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return an OK when the calc date does not equal the stored start date" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorp))))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockReturnsService.retrieveMandatoryDates(any(), any(), any()))
        .thenReturn(Future.successful(MandatoryDateModel(LocalDate.of(2018, 1, 1), Some(testDate), Some(DateSelection.calculated_date))))


      callAuthorised(testController.mandatoryStartPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitMandatoryStart" should {
    val fakeRequest = FakeRequest(features.returns.routes.ReturnsController.submitMandatoryStart())

    "redirect to the company bank account page if confirmed without an incorp date" in new Setup {
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "redirect to the company bank account page, if they are incorped and select the calculated date" in new Setup {
      when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
        .thenReturn(Future.successful(LocalDate.now()))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "calculated_date"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "redirect to the company bank account page, if they are incorped and select a custom date" in new Setup {
      when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
        .thenReturn(Future.successful(LocalDate.now()))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val incorpDatePlusTwo: LocalDate = currentProfile.incorporationDate.get.plusDays(2)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "specific_date",
        "startDate.month" -> incorpDatePlusTwo.getMonthValue.toString,
        "startDate.year" -> incorpDatePlusTwo.getYear.toString,
        "startDate.day" -> incorpDatePlusTwo.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "return a bad request, if they are incorped and select a custom date before the incorp date" in new Setup {
      when(mockReturnsService.retrieveCalculatedStartDate(any(), any(), any()))
        .thenReturn(Future.successful(LocalDate.now()))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val incorpDatePlusTwo: LocalDate = currentProfile.incorporationDate.get.minusDays(2)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "specific_date",
        "startDate.month" -> incorpDatePlusTwo.getMonthValue.toString,
        "startDate.year" -> incorpDatePlusTwo.getYear.toString,
        "startDate.day" -> incorpDatePlusTwo.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitMandatoryStart, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "voluntaryStartPage" should {
    val dateSelection = (DateSelection.calculated_date, Some(LocalDate.now()))
    val calcDate = LocalDate.now()
    val voluntaryViewModel = VoluntaryPageViewModel(Some(dateSelection),Some(calcDate))
    "return OK when the company is incorporated, returns are present and calc date equals stored date" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorp))))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))

      when(mockReturnsService.voluntaryStartPageViewModel(any())(any(), any(), any()))
        .thenReturn(Future.successful(voluntaryViewModel))

      callAuthorised(testController.voluntaryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when the company is not incorporated" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorp))))

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))

      when(mockReturnsService.voluntaryStartPageViewModel(any())(any(), any(), any()))
        .thenReturn(Future.successful(voluntaryViewModel))

      callAuthorised(testController.voluntaryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return OK when the company is not incorporated and there is nothing to prepop" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = None)))

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))

      when(mockReturnsService.voluntaryStartPageViewModel(any())(any(), any(), any()))
        .thenReturn(Future.successful(voluntaryViewModel))

      callAuthorised(testController.voluntaryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return an OK when returns are not present" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))

      when(mockReturnsService.voluntaryStartPageViewModel(any())(any(), any(), any()))
        .thenReturn(Future.successful(voluntaryViewModel))

      callAuthorised(testController.voluntaryStartPage) { result =>
        status(result) mustBe OK
      }
    }

    "return an OK when the specific date does not equal the incorp date" in new Setup {
      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(start = Some(startAtIncorpMinusTwo))))

      mockWithCurrentProfile(Some(currentProfile))

      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))

      when(mockReturnsService.voluntaryStartPageViewModel(any())(any(), any(), any()))
        .thenReturn(Future.successful(voluntaryViewModel))

      callAuthorised(testController.voluntaryStartPage) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submitVoluntaryStartPage" should {

    val date = LocalDate.of(2017, 1, 1)
    val returns = Returns(Some(true), Some(Frequency.quarterly), Some(Stagger.jan), Some(Start(Some(date))))
    val fakeRequest = FakeRequest(features.returns.routes.ReturnsController.submitVoluntaryStart())
    val incorpDatePlusTwo: LocalDate = currentProfile.incorporationDate.get.plusDays(2)

    "redirect to the company bank account page if they select the date of incorp being not incorped" in new Setup {
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.company_registration_date
      )

      when(mockReturnsService.retrieveCTActiveDate(any(), any(), any()))
        .thenReturn(Future.successful(Some(LocalDate.of(2017, 1, 1))))

      when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(returns))

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "redirect to the company bank account page if they submit a valid start date without being incorped" in new Setup {
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val nowPlusFive: LocalDate = LocalDate.now().plusDays(5)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.specific_date,
        "startDate.month" -> nowPlusFive.getMonthValue.toString,
        "startDate.year" -> nowPlusFive.getYear.toString,
        "startDate.day" -> nowPlusFive.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      when(mockReturnsService.saveVoluntaryStartDate(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(returns))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "provide a bad request if they submit an invalid start date without being incorped" in new Setup {
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val nowMinusFive: LocalDate = LocalDate.now().minusDays(5)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> DateSelection.specific_date,
        "startDate.month" -> nowMinusFive.getMonthValue.toString,
        "startDate.year" -> nowMinusFive.getYear.toString,
        "startDate.day" -> nowMinusFive.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile.copy(incorporationDate = None)))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to the company bank account page, if they are incorped and select the incorp date" in new Setup {
      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "company_registration_date"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "redirect to the company bank account page, if they are incorped and select the start of business date" in new Setup {
      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "business_start_date"
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "redirect to the company bank account page, if they are incorped and select a custom date" in new Setup {
      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "specific_date",
        "startDate.month" -> incorpDatePlusTwo.getMonthValue.toString,
        "startDate.year" -> incorpDatePlusTwo.getYear.toString,
        "startDate.day" -> incorpDatePlusTwo.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-vat/business-bank-account")
      }
    }

    "return a bad request, if they are incorped and select a custom date before the incorp date" in new Setup {
      when(mockPrePopService.getCTActiveDate(any(), any()))
        .thenReturn(OptionT[Future, LocalDate](Future.successful(currentProfile.incorporationDate)))
      when(mockReturnsService.saveVatStartDate(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val incorpDateMinusTwo: LocalDate = currentProfile.incorporationDate.get.minusDays(2)
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> "specific_date",
        "startDate.month" -> incorpDateMinusTwo.getMonthValue.toString,
        "startDate.year" -> incorpDateMinusTwo.getYear.toString,
        "startDate.day" -> incorpDateMinusTwo.getDayOfMonth.toString
      )

      mockWithCurrentProfile(Some(currentProfile))

      submitAuthorised(testController.submitVoluntaryStart, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
