/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import common.enums.VatRegStatus
import models.CurrentProfile
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{SessionProfile, SessionService}
import testHelpers.VatRegSpec

import scala.concurrent.{ExecutionContext, Future}


class SessionProfileSpec extends VatRegSpec {

  class Setup {
    val sessionProfile: SessionProfile = new SessionProfile {
      override implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
      override val sessionService: SessionService = mockSessionService
    }
  }

  def testFunc: Future[Result] = Future.successful(Ok)

  implicit val request = FakeRequest()

  val validProfile = CurrentProfile("testRegId", VatRegStatus.draft)

  "calling withCurrentProfile" should {
    "redirect to the welcome show if the current profile was not fetched from keystore" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", None)
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat")
    }
    "perform the passed in function" when {
      "the ct status is not present in the current profile" in new Setup {
        mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile))
        val result = sessionProfile.withCurrentProfile() { _ => testFunc }
        status(result) mustBe OK
      }
      "the ct status does not equal a status 06" in new Setup {
        mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile))
        val result = sessionProfile.withCurrentProfile() { _ => testFunc }
        status(result) mustBe OK
      }
      "the vat status is held but checkStatus is set to false" in new Setup {
        mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)))
        val result = sessionProfile.withCurrentProfile(checkStatus = false) { _ => testFunc }
        status(result) mustBe OK
      }
    }
    "redirect to the application submitted page if the status is submitted" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)))
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/application-submitted")
    }
    "redirect to the submission in progress page if the status is locked" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.locked)))
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/submission-in-progress")
    }
    "redirect to the submission failed page if the status is failed" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.failed)))
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/something-went-wrong")
    }
    "redirect to the retry submission page if the status is failedRetryable" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.failedRetryable)))
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/submission-failure")
    }
    "redirect to the already submitted page if the status is duplicateSubmission" in new Setup {
      mockSessionFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile.copy(vatRegistrationStatus = VatRegStatus.duplicateSubmission)))
      val result = sessionProfile.withCurrentProfile() { _ => testFunc }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/already-submitted")
    }
  }
}
