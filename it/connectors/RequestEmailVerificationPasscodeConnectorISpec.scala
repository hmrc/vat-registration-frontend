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
import models.external.{AlreadyVerifiedEmailAddress, RequestEmailPasscodeSuccessful}
import play.api.test.Helpers._
import support.AppAndStubs

class RequestEmailVerificationPasscodeConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching {

  lazy val connector: RequestEmailVerificationPasscodeConnector = app.injector.instanceOf[RequestEmailVerificationPasscodeConnector]

  lazy val testEmail: String = "test@test.com"

  "requestEmailVerificationPasscode" should {
    "return a success response" when {
      "the feature switch is enabled and successfully calls the stub" in {
        enable(StubEmailVerification)

        stubPost("/register-for-vat/test-only/api/request-passcode", CREATED, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail))

        res mustBe RequestEmailPasscodeSuccessful
      }
    }

    "return a already verified response" when {
      "the feature switch is enabled and the stub returns Conflict" in {
        enable(StubEmailVerification)

        stubPost("/register-for-vat/test-only/api/request-passcode", CONFLICT, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail))

        res mustBe AlreadyVerifiedEmailAddress
      }
    }

    "return a success response" when {
      "the feature switch is disabled and the request email passcode API is successful" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/request-passcode", CREATED, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail))

        res mustBe RequestEmailPasscodeSuccessful
      }
    }

    "return a already verified response" when {
      "the feature switch is disabled and the request email passcode API returns Conflict" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/request-passcode", CONFLICT, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail))

        res mustBe AlreadyVerifiedEmailAddress
      }
    }
  }
}
