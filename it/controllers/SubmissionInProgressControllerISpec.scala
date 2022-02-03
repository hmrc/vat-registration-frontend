/*
 * Copyright 2020 HM Revenue & Customs
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

import common.enums.VatRegStatus
import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class SubmissionInProgressControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.routes.SubmissionInProgressController.show.url

  val testAckRef = "VRN1234567"

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST $url" must {
    "return a redirect to Submission Successful page if the status is submitted" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.regStatus(VatRegStatus.submitted)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationSubmissionController.show.url)
      }
    }

    "return a redirect to Submission In Progress page if the status is locked" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.regStatus(VatRegStatus.locked)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.SubmissionInProgressController.show.url)
      }
    }

    "return a redirect to Already Submitted page if the status is duplicateSubmission" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.regStatus(VatRegStatus.duplicateSubmission)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ErrorController.alreadySubmitted.url)
      }
    }

    "return a redirect to Submission Failed page if the status is failed" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.regStatus(VatRegStatus.failed)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ErrorController.submissionFailed.url)
      }
    }

    "return a redirect to Submission Retryable page if the status is failedRetryable" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.regStatus(VatRegStatus.failedRetryable)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ErrorController.submissionRetryable.url)
      }
    }
  }
}
