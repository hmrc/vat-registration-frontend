/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.errors

import fixtures.VatRegistrationFixture
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.errors._

class ErrorControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  val mockSubmissionFailedView = app.injector.instanceOf[SubmissionFailed]
  val mockSubmissionRetryableView = app.injector.instanceOf[SubmissionRetryableView]
  val mockAlreadySubmittedView = app.injector.instanceOf[AlreadySubmittedKickout]
  val contactView = app.injector.instanceOf[ContactView]
  val missingAnswerView = app.injector.instanceOf[MissingAnswer]

  val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  trait Setup {
    val testErrorController: ErrorController = new ErrorController(
      mockAuthClientConnector,
      mockSessionService,
      mockSubmissionFailedView,
      mockSubmissionRetryableView,
      mockAlreadySubmittedView,
      contactView,
      missingAnswerView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "ErrorController" should {
    "return the SubmissionFailed view" in new Setup {
      callAuthorised(testErrorController.submissionFailed, useBasicAuth = false) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return the SubmissionRetryable view" in new Setup {
      callAuthorised(testErrorController.submissionRetryable, useBasicAuth = false) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return the AlreadySubmitted view" in new Setup {
      callAuthorised(testErrorController.alreadySubmitted, useBasicAuth = false) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return the SignOut view" in new Setup {
      callAuthorised(testErrorController.alreadySubmittedSignOut, useBasicAuth = false) {
        result =>
          status(result) mustBe SEE_OTHER
          header(LOCATION, result) mustBe Some(controllers.callbacks.routes.SignInOutController.signOut.url)
      }
    }
    "return the Contact view" in new Setup {
      callAuthorised(testErrorController.contact, useBasicAuth = false) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) mustBe contactView()(FakeRequest(), appConfig, messages).body
      }
    }
    "return the Missing Answer view" in new Setup {
      val answer = "test"
      mockSessionFetchAndGet("missingAnswer", Some(answer))
      callAuthorised(testErrorController.missingAnswer, useBasicAuth = false) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) mustBe missingAnswerView(answer)(messages, FakeRequest(), appConfig).body
      }
    }
  }
}