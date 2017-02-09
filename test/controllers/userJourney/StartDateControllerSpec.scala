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

import builders.AuthBuilder
import enums.CacheKeys
import helpers.VatRegSpec
import models.view.StartDate
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class StartDateControllerSpec extends VatRegSpec {

  object TestStartDateController extends StartDateController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.StartDateController.show()}" should {

    "return HTML when there's a start date in S4L" in {
      val startDate = StartDate(StartDate.SPECIFIC_DATE, Some("2017"), Some("01"), Some("30"))

      when(mockS4LService.fetchAndGet[StartDate](Matchers.eq(CacheKeys.StartDate.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(startDate)))

      callAuthorised(TestStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[StartDate](Matchers.eq(CacheKeys.StartDate.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      callAuthorised(TestStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("start date")
      }
    }
  }

  s"POST ${routes.StartDateController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.StartDateController.submit()} with valid data" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(StartDate.empty)))

      when(mockS4LService.saveForm[StartDate](Matchers.eq(CacheKeys.StartDate.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "startDateRadio" -> StartDate.COMPANY_REGISTRATION_DATE
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
      }

    }
  }

  s"GET ${routes.StartDateController.showMandatoryStartDateConfirmation()}" should {

    "display the mandatory start date confirmation page to the user" in {
      callAuthorised(TestStartDateController.showMandatoryStartDateConfirmation, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }
  }

  s"POST ${routes.StartDateController.submitMandatoryStartDateConfirmation()}" should {

    "redirect the user to the trading name page after clicking continue on the mandatory start date confirmation page" in {
      callAuthorised(TestStartDateController.submitMandatoryStartDateConfirmation, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
      }
    }
  }

}
