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

package connectors

import featureswitch.core.config.{FeatureSwitching, StubEmailVerification}
import itutil.IntegrationSpecBase
import models.external.{EmailAlreadyVerified, EmailVerifiedSuccessfully, PasscodeMismatch, PasscodeNotFound}
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, Upstream5xxResponse}

class VerifyEmailVerificationPasscodeConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching {

  lazy val connector: VerifyEmailVerificationPasscodeConnector = app.injector.instanceOf[VerifyEmailVerificationPasscodeConnector]

  lazy val testPasscode: String = "123456"
  lazy val testEmail: String = "test@test.com"

  "verifyEmailVerificationPasscode" should {
    "return a successful response" when {
      "the feature switch is enabled ans the stub returns Created" in {
        enable(StubEmailVerification)

        stubPost("/register-for-vat/test-only/api/verify-passcode", CREATED, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe EmailVerifiedSuccessfully
      }
    }

    "return already verified response" when {
      "the feature switch is enabled ans the stub returns NoContent" in {
        enable(StubEmailVerification)

        stubPost("/register-for-vat/test-only/api/verify-passcode", NO_CONTENT, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe EmailAlreadyVerified
      }
    }

    "return a successful response" when {
      "the feature switch is disabled and the email verification API returns Created" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/verify-passcode", CREATED, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe EmailVerifiedSuccessfully
      }
    }

    "return already verified response" when {
      "the feature switch is disabled and the email verification API returns NoContent" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/verify-passcode", NO_CONTENT, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe EmailAlreadyVerified
      }
    }

    "return passcode mismatch response" when {
      "the feature switch is disabled and the email verification API returns PasscodeMismatch" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe PasscodeMismatch
      }
    }

    "return passcode not found response" when {
      "the feature switch is disabled and the email verification API returns PasscodeNotFound" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res mustBe PasscodeNotFound
      }
    }

    "return unexpected response" when {
      "the feature switch is disabled and the email verification API returns InternalServerException" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/verify-passcode", INTERNAL_SERVER_ERROR, "")

        intercept[InternalServerException] {
          await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))
        }
      }
    }
  }

}
