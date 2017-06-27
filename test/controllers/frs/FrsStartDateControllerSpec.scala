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
import common.Now
import fixtures.VatRegistrationFixture
import forms.frs.FrsStartDateFormFactory
import helpers.{S4LMockSugar, VatRegSpec}
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

  val today: LocalDate = LocalDate.now

  val frsStartDateFormFactory = new FrsStartDateFormFactory(mockDateService, Now[LocalDate](today))

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
      save4laterReturnsViewModel(validStartDateView)()

      submitAuthorised(
        FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {
      save4laterReturnsViewModel(validStartDateView)()

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

    "return 303 with VAT Registration Date selected" in {
      save4laterReturnsViewModel(validStartDateView)()
      save4laterExpectsSave[FrsStartDateView]()

      submitAuthorised(FrsTestStartDateController.submit(), fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FrsStartDateView.VAT_REGISTRATION_DATE
      )) {
        _ redirectsTo s"$contextRoot/trading-name"
      }
    }


  }
}
