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
import helpers.VatRegSpec
import models.StartDateModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class StartDateControllerSpec extends VatRegSpec {

  object TestStartDateController extends StartDateController(ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.StartDateController.show()}" should {

    "return HTML" in {
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
      AuthBuilder.submitWithAuthorisedUser(TestStartDateController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "startDate" -> StartDateModel.WHEN_REGISTERED
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
      }

    }
  }

}
