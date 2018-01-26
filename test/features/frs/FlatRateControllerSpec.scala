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

package controllers

import java.time.LocalDate

import common.Now
import connectors.KeystoreConnector
import features.turnoverEstimates.TurnoverEstimates
import fixtures.VatRegistrationFixture
import forms.FrsStartDateFormFactory
import helpers.{ControllerSpec, MockMessages}
import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.FlatRateService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class FlatRateControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {
  val today: LocalDate = LocalDate.of(2017, 3, 21)
  val frsStartDateFormFactory = new FrsStartDateFormFactory(mockDateService, Now[LocalDate](today))

  implicit val localDateOrdering = frsStartDateFormFactory.LocalDateOrdering
  implicit val fixedToday: Now[LocalDate] = Now[LocalDate](today)

  trait Setup {
    val controller: FlatRateController = new FlatRateController {
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val flatRateService: FlatRateService = mockFlatRateService
      override val startDateForm: Form[FrsStartDateView] = frsStartDateFormFactory.form()
    }

    mockAllMessages
  }

  class SubmissionSetup extends Setup {
    when(mockFlatRateService.businessSectorView()(any(), any()))
      .thenReturn(Future.successful(validBusinessSectorView))
  }

  s"GET ${routes.FlatRateController.annualCostsInclusivePage()}" should {

    "return a 200 when a previously completed S4LFlatRateScheme is returned" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      callAuthorised(controller.annualCostsInclusivePage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return a 200 when an empty S4LFlatRateScheme is returned from the service" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(S4LFlatRateScheme()))

      callAuthorised(controller.annualCostsInclusivePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualInclusiveCosts()}" should {

    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualInclusiveCosts())

    "return 400 with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val emptyRequest: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualInclusiveCosts(), emptyRequest){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Annual Costs Inclusive selected Yes" in new Setup {

      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveAnnualCostsInclusive(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "return 303 with Annual Costs Inclusive selected within 12 months" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveAnnualCostsInclusive(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "skip next question if 2% of estimated taxable turnover <= 1K and NO answered" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.isOverLimitedCostTraderThreshold(any(), any()))
        .thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/confirm-business-type")
      }
    }

    "redirect to next question if 2% of estimated taxable turnover > 1K and NO answered" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.isOverLimitedCostTraderThreshold(any(), any()))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/spends-less-than-two-percent-of-turnover-a-year-on-goods")
      }
    }
  }

  val estimateVatTurnover = TurnoverEstimates(1000000L)

  val s4LFlatRateSchemeNoAnnualCostsLimited: S4LFlatRateScheme = validS4LFlatRateScheme.copy(annualCostsLimited = None)

  s"GET ${routes.FlatRateController.annualCostsLimitedPage()}" should {

    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is not found on the vat scheme" in new Setup {

      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(s4LFlatRateSchemeNoAnnualCostsLimited))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      callAuthorised(controller.annualCostsLimitedPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is found on the vat scheme" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      callAuthorised(controller.annualCostsLimitedPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualCostsLimited()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualCostsLimited())

    "return a 400 when the request is empty" in new Setup {

      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return a 303 when AnnualCostsLimitedView.selected is Yes" in new Setup{
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      when(mockFlatRateService.saveAnnualCostsLimited(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES
      )

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "return 303 when AnnualCostsLimitedView.selected is Yes within 12 months" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      when(mockFlatRateService.saveAnnualCostsLimited(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES_WITHIN_12_MONTHS
      )

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/use-limited-cost-business-flat-rate")
      }
    }

    "return a 303 and redirect to confirm business sector with Annual Costs Limited selected No" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.getFlatRateSchemeThreshold()(any(), any()))
        .thenReturn(Future.successful(1000L))

      when(mockFlatRateService.saveAnnualCostsLimited(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      private val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.NO
      )

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/confirm-business-type")
      }
    }
  }

  s"GET ${routes.FlatRateController.confirmSectorFrsPage()}" should {

    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(controller.confirmSectorFrsPage()){ result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.FlatRateController.submitConfirmSectorFrs()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitConfirmSectorFrs())

    "works with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      when(mockFlatRateService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitConfirmSectorFrs, request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/your-flat-rate")
      }
    }
  }

  s"GET ${routes.FlatRateController.frsStartDatePage()}" should {

    "return HTML when there's a frs start date in S4L" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      val frsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(LocalDate.now))

      callAuthorised(controller.frsStartDatePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      callAuthorised(controller.frsStartDatePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitFrsStartDate()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitFrsStartDate())

    "return 400 when no data posted" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> "1",
        "frsStartDate.month" -> "",
        "frsStartDate.year" -> "2017"
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 with Different Date selected and date that is less than 2 working days in the future" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> "20",
        "frsStartDate.month" -> "3",
        "frsStartDateDate.year" -> "2017"
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 with VAT Registration Date selected" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveFRSStartDate(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.joinFrsPage()}" should {

    "render the page" when {

      "visited for the first time" in new Setup {

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(S4LFlatRateScheme()))

        callAuthorised(controller.joinFrsPage()) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }

      "user has already answered this question" in new Setup {

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
          .thenReturn(Future.successful(validS4LFlatRateScheme))

        callAuthorised(controller.joinFrsPage) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }
    }
  }

  s"POST ${routes.FlatRateController.submitJoinFRS()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitJoinFRS())

    "return 400 with Empty data" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(controller.submitJoinFRS(), fakeRequest.withFormUrlEncodedBody())(result =>
        status(result) mustBe 400
      )
    }

    "return 303 with Join Flat Rate Scheme selected Yes" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoinFRS(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "true"
      )
      submitAuthorised(controller.submitJoinFRS(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(s"$contextRoot/spends-less-including-vat-on-goods")
      }
    }

    "return 303 with Join Flat Rate Scheme selected No" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoinFRS(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "false"
      )

      submitAuthorised(controller.submitJoinFRS(), request){ result =>
        redirectLocation(result) mustBe Some(s"$contextRoot/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.registerForFrsPage()}" should {

    "return a 200 and render the page" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      callAuthorised(controller.registerForFrsPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitRegisterForFrs()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitRegisterForFrs())

    "return 400 with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitRegisterForFrs(), request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "true"
      )

      submitAuthorised(controller.submitRegisterForFrs(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/flat-rate-scheme-join-date")
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.saveFRSStartDate(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "false"
      )

      submitAuthorised(controller.submitRegisterForFrs(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.yourFlatRatePage()}" should {

    "return a 200 and render the page" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      callAuthorised(controller.yourFlatRatePage()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitYourFlatRate()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitYourFlatRate())

    "return 400 with Empty data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      when(mockFlatRateService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "true"
      )

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/flat-rate-scheme-join-date")
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockFlatRateService.businessSectorView()(any(), any()))
        .thenReturn(Future.successful(validBusinessSectorView))

      when(mockFlatRateService.saveBusinessSector(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      when(mockFlatRateService.saveRegisterForFRS(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "false"
      )

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }
}
