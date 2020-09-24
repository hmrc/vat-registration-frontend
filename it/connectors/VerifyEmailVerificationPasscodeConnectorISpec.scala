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
import models.external.{EmailAlreadyVerified, EmailVerifiedSuccessfully}
import play.api.test.Helpers._
import support.AppAndStubs

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
  }

}
