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

package services

import common.enums.VatRegStatus
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import models.CurrentProfile
import play.api.i18n.Messages
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import testHelpers.VatRegSpec

import scala.concurrent.Future

class SessionProfileSpec extends VatRegSpec with VatRegistrationFixture {
  val mockMessages = mock[Messages]

  class Setup {
    val sp = new SessionProfile {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "hasIvStatus" should {
    "return 200 when CurrentProfile ivPassed is true" in new Setup {
      val cp = CurrentProfile("", VatRegStatus.draft)
      val res = await(sp.ivPassedCheck(Future.successful(Ok))(cp, FakeRequest(), mockMessages))
      res.header.status mustBe 200
    }
  }
}
