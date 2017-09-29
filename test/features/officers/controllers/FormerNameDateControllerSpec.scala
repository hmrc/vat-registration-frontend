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

package controllers.vatLodgingOfficer

import java.time.LocalDate

import common.Now
import controllers.vatLodgingOfficer
import fixtures.VatRegistrationFixture
import forms.vatLodgingOfficer.FormerNameDateForm
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.view.vatLodgingOfficer.{FormerNameDateView, FormerNameView}
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class FormerNameDateControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val today: LocalDate = LocalDate.of(2017, 3, 21)
  val formerNameDateForm = FormerNameDateForm
  implicit val localDateOrdering = formerNameDateForm
  val validFormerNameView = FormerNameView(true, Some("FORMER_NAME"))

  object FormerNameDateController extends FormerNameDateController(ds)(mockS4LService, mockVatRegistrationService) {
    implicit val fixedToday = Now[LocalDate](today)
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.FormerNameDateController.show())

  s"GET ${vatLodgingOfficer.routes.FormerNameDateController.show()}" should {
    "return HTML when a date no data entered in S4L for Former name" in {
      val formerNameDate = FormerNameDateView(LocalDate.now)
      save4laterReturnsNoViewModel[FormerNameView]()
      save4laterReturnsViewModel[FormerNameDateView](formerNameDate)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(),any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(FormerNameDateController.show) {
        _ includesText ("When did you change it from")
      }
    }

    "return HTML when a date has been entered in S4L and None Former name " in {
      val formerNameDate = FormerNameDateView(LocalDate.now)
      save4laterReturnsNoViewModel[FormerNameView]()
      save4laterReturnsViewModel[FormerNameDateView](formerNameDate)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      save4laterReturnsViewModel(formerNameDate)()

      callAuthorised(FormerNameDateController.show) {
        _ includesText ("When did you change it from")
      }
    }

    "return HTML when there's nothing in S4L, No Former name and vatScheme contains data" in {
      save4laterReturnsNoViewModel[FormerNameDateView]()
      save4laterReturnsViewModel[FormerNameView](validFormerNameView)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(),any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(FormerNameDateController.show) {
        _ includesText ("When did you change it from")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[FormerNameDateView]()
      save4laterReturnsViewModel[FormerNameView](validFormerNameView)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(),any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(FormerNameDateController.show) {
        _ includesText ("When did you change it from")
      }
    }
  }

  s"POST ${routes.FormerNameDateController.submit()}" should {

    "return 400 when no data posted" in {
      save4laterReturnsNoViewModel[FormerNameDateView]()
      save4laterReturnsNoViewModel[FormerNameView]()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      submitAuthorised(
        FormerNameDateController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {
      save4laterReturnsViewModel[FormerNameDateView](FormerNameDateView(LocalDate.now))()
      save4laterReturnsViewModel[FormerNameView](validFormerNameView)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      when(mockVatRegistrationService.getVatScheme()(any(), any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      submitAuthorised(
        FormerNameDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "formerNameDate.day" -> "1",
          "formerNameDate.month" -> "",
          "formerNameDate.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 303 with Former name Date selected" in {
      save4laterReturnsViewModel[FormerNameDateView](FormerNameDateView(LocalDate.now))()
      save4laterReturnsViewModel[FormerNameView](validFormerNameView)()
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterExpectsSave[FormerNameDateView]()

      submitAuthorised(
        FormerNameDateController.submit(), fakeRequest.withFormUrlEncodedBody(
          "formerNameDate.day" -> "1",
          "formerNameDate.month" -> "1",
          "formerNameDate.year" -> "2017"
        )) {
        _ redirectsTo s"$contextRoot/your-contact-details"
      }

    }
  }
}