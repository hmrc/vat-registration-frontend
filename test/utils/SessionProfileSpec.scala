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

package utils

import common.enums.VatRegStatus
import connectors.KeystoreConnect
import helpers.VatRegSpec
import models.CurrentProfile
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import services.SessionProfile
import play.api.test.Helpers._

import scala.concurrent.Future


class SessionProfileSpec extends VatRegSpec {

  object TestSession extends SessionProfile {
    override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
  }

  def testFunc : Future[Result] = Future.successful(Ok)
  implicit val request = FakeRequest()

  def validProfile(status: Option[String]) = CurrentProfile("testName", "testRegId", "testTransId", VatRegStatus.draft, None, Some(true), status)

  "calling withCurrentProfile" should {
    "redirect to the welcome show if the current profile was not fetched from keystore" in {
      mockKeystoreFetchAndGet[CurrentProfile]("CurrentProfile", None)
      val result = TestSession.withCurrentProfile { _ => testFunc }
      status(result) mustBe 303
      redirectLocation(result) mustBe Some("/register-for-vat")
    }
    "perform the passed in function" when {
      "the ct status is not present in the current profile" in {
        mockKeystoreFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile(None)))
        val result = TestSession.withCurrentProfile { _ => testFunc }
        status(result) mustBe OK
      }
      "the ct status does not equal a status 06" in {
        mockKeystoreFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile(Some("04"))))
        val result = TestSession.withCurrentProfile { _ => testFunc }
        status(result) mustBe OK
      }
    }
    "Redirect to the post sign in page if the ct status equals 06" in {
      mockKeystoreFetchAndGet[CurrentProfile]("CurrentProfile", Some(validProfile(Some("06"))))
      val result = TestSession.withCurrentProfile { _ => testFunc }
      status(result) mustBe 303
      redirectLocation(result) mustBe Some("/register-for-vat/post-sign-in")
    }
  }
}
