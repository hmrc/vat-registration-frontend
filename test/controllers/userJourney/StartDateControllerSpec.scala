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

package controllers.userJourney

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import builders.AuthBuilder
import common.Now
import controllers.userJourney.vatChoice.StartDateController
import fixtures.VatRegistrationFixture
import forms.vatDetails.vatChoice.StartDateFormFactory
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.StartDateView
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DateService, VatRegistrationService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class StartDateControllerSpec extends VatRegSpec with VatRegistrationFixture {
  
  val today: LocalDate = LocalDate.of(2017, 3, 21)

  val mockVatRegistrationService = mock[VatRegistrationService]

  val mockDateService = mock[DateService]
  when(mockDateService.addWorkingDays(Matchers.eq(today), anyInt())).thenReturn(today.plus(2, DAYS))

  val startDateFormFactory = new StartDateFormFactory(mockDateService, Now[LocalDate](today))

  object TestStartDateController extends StartDateController(startDateFormFactory, ds)(mockS4LService, mockVatRegistrationService) {
    implicit val fixedToday = Now[LocalDate](today)
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatChoice.routes.StartDateController.show())

  s"GET ${vatChoice.routes.StartDateController.show()}" should {

    "return HTML when there's a start date in S4L" in {
      val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

      when(mockS4LService.fetchAndGet[StartDateView]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(startDate)))

      callAuthorised(TestStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[StartDateView]()
        (Matchers.eq(S4LKey[StartDateView]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[StartDateView]()
        (Matchers.eq(S4LKey[StartDateView]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }
  }

  s"POST ${vatChoice.routes.StartDateController.submit()} with Empty data" should {

    "return 400 when no data posted" in {
      AuthBuilder.submitWithAuthorisedUser(
        TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {
      AuthBuilder.submitWithAuthorisedUser(
        TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
          "startDateRadio" -> StartDateView.SPECIFIC_DATE,
          "startDate.day" -> "1",
          "startDate.month" -> "",
          "startDate.year" -> "2017"
        )) {
        status(_) mustBe Status.BAD_REQUEST
      }
    }
  }

  s"POST ${vatChoice.routes.StartDateController.submit()} with valid data" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(StartDateView())))

      when(mockS4LService.saveForm[StartDateView](any[StartDateView]())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.COMPANY_REGISTRATION_DATE,
        "startDate.day" -> "21",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/trading-name"
      }
    }
  }

  s"POST ${vatChoice.routes.StartDateController.submit()} with StartDate having a specific date" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(StartDateView())))

      when(mockS4LService.saveForm[StartDateView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDateView.SPECIFIC_DATE,
        "startDate.day" -> "24",
        "startDate.month" -> "3",
        "startDate.year" -> "2017"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/trading-name"
      }
    }
  }
}
