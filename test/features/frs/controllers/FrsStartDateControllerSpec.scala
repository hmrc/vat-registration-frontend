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

package controllers.frs


import java.time.LocalDate

import common.Now
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import forms.frs.FrsStartDateFormFactory
import helpers.{ControllerSpec, MockMessages}
import models.view.frs.FrsStartDateView
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class FrsStartDateControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  val today: LocalDate = LocalDate.of(2017, 3, 21)
  val frsStartDateFormFactory = new FrsStartDateFormFactory(mockDateService, Now[LocalDate](today))

  //implicit val localDateOrdering = frsStartDateFormFactory.LocalDateOrdering

  implicit val fixedToday: Now[LocalDate] = Now[LocalDate](today)

  trait Setup {
    val controller: FrsStartDateController = new FrsStartDateController{
      override val service: VatRegistrationService = mockVatRegistrationService
      override val startDateForm: Form[FrsStartDateView] = frsStartDateFormFactory.form()
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }

    mockAllMessages
  }

  val fakeRequest = FakeRequest(routes.FrsStartDateController.show())

  s"GET ${routes.FrsStartDateController.show()}" should {

    "return HTML when there's a frs start date in S4L" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      val frsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(LocalDate.now))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme.copy(frsStartDate = None)))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FrsStartDateController.submit()}" should {

    "return 400 when no data posted" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> "1",
        "frsStartDate.month" -> "",
        "frsStartDate.year" -> "2017"
      )

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 with Different Date selected and date that is less than 2 working days in the future" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
        "frsStartDate.day" -> "20",
        "frsStartDate.month" -> "3",
        "frsStartDateDate.year" -> "2017"
      )

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 with VAT Registration Date selected" in new Setup {
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.saveFRSStartDate(any())(any(), any()))
        .thenReturn(Future.successful(Left(validS4LFlatRateScheme)))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE
      )

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }

//    "return 303 with Different Date entered" in new Setup {
//
//      mockWithCurrentProfile(Some(currentProfile))
//
////      when(mockDateService.addWorkingDays(any(), any())).thenReturn(fixedToday())
//
//      val minDate: LocalDate = LocalDate.now().plusDays(30)
//
//      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
//        "frsStartDateRadio" -> FrsStartDateView.DIFFERENT_DATE,
//        "frsStartDate.day" -> minDate.getDayOfMonth.toString,
//        "frsStartDate.month" -> minDate.getMonthValue.toString,
//        "frsStartDate.year" -> minDate.getYear.toString
//      )
//
//      submitAuthorised(controller.submit(), request) { result =>
//        status(result) mustBe 303
//        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
//      }
//    }
  }
}
