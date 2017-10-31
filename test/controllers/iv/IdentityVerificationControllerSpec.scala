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

package controllers.iv

import connectors.KeystoreConnector
import helpers.VatRegSpec
import models.CurrentProfile
import org.mockito.Matchers
import org.mockito.Mockito.when
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class IdentityVerificationControllerSpec extends VatRegSpec {
  val testController = new IdentityVerificationController(ds, mockIdentityVerificationConnector) {
    override val authConnector: AuthConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  "GET timeoutIV" should {
    "display the timeout IV error page" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(testController.timeoutIV)(_ includesText "Your session has timed out due to inactivity")
    }
  }

  "GET unableToConfirmIdentity" should {
    "display the Unable to confirme identity error page" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(testController.unableToConfirmIdentity)(_ includesText "t confirm your identity")
    }
  }

  "GET failedIV" should {
    "display the Failed IV error page" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(testController.failedIV)(_ includesText "t confirm your identity")
    }
  }

  "GET lockedOut" should {
    "display the Locked out error page" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(testController.lockedOut)(_ includesText "ve been locked out")
    }
  }

  "GET userAborted" should {
    "display the User aborted error page" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(testController.userAborted)(_ includesText "t completed the identity check")
    }
  }
}
