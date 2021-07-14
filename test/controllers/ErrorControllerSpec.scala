/*
 * Copyright 2021 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.pages.error.{submissionFailed, SubmissionRetryableView}

class ErrorControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  val mockSubmissionFailedView = app.injector.instanceOf[submissionFailed]
  val mockSubmissionRetryableView = app.injector.instanceOf[SubmissionRetryableView]

  trait Setup {
    val testErrorController: ErrorController = new ErrorController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockSubmissionFailedView,
      mockSubmissionRetryableView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "ErrorController" should {
    "return the SubmissionFailed view" in new Setup {
      callAuthorised(testErrorController.submissionFailed) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return the SubmissionRetryable view" in new Setup {
      callAuthorised(testErrorController.submissionRetryable) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
  }
}
