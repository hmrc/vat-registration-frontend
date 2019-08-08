/*
 * Copyright 2019 HM Revenue & Customs
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

package features.officer.controllers

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import common.enums.IVResult
import connectors.KeystoreConnector
import features.officer.services.IVService
import helpers.{ControllerSpec, MockMessages}
import mocks.AuthMock
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class IdentityVerificationControllerSpec extends ControllerSpec with MockMessages {
  trait Setup {
    val testController = new IdentityVerificationController {
      val ivService: IVService = mockIVService
      val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      val messagesApi: MessagesApi = mockMessagesAPI
      val authConnector: AuthConnector = mockAuthClientConnector
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile.copy(ivPassed = None)))
  }

  "redirectToIV" should {
    "redirect to link returned from IV Proxy" in new Setup {
      when(mockIVService.setupAndGetIVJourneyURL(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful("/test-redirect-uri"))

      callAuthorised(testController.redirectToIV()) { res =>
        redirectLocation(res)(Timeout(FiniteDuration(5, TimeUnit.SECONDS))) mustBe Some("/test-redirect-uri")
      }
    }

    "throw error if no link is returned" in new Setup {
      when(mockIVService.setupAndGetIVJourneyURL(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception))

      callAuthorised(testController.redirectToIV()) { res =>
        redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
        status(res) mustBe 303
      }
    }
  }

  "completedIVJourney" should {
    "return 303 when the user has a journeyID in S4L and outcome is success" in new Setup {
      when(mockIVService.fetchAndSaveIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.Success))

      callAuthorised(testController.completedIVJourney) {
        redirectLocation(_) mustBe Some(features.officer.controllers.routes.OfficerController.showFormerName().url)
      }
    }
    "return 500 when the user does not have journeyID/response from iv" in new Setup {
      when(mockIVService.fetchAndSaveIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse("Error occurred while updating IV Status", 500, 500)))

      callAuthorised(testController.completedIVJourney) { res =>
        redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
        status(res) mustBe 303
      }
    }
  }

  "GET timeoutIV" should {
    "display the timeout IV error page" in new Setup {
      callAuthorised(testController.timeoutIV)(contentAsString(_) must include(MOCKED_MESSAGE))
    }
  }

  "GET unableToConfirmIdentity" should {
    "display the Unable to confirme identity error page" in new Setup {
      callAuthorised(testController.unableToConfirmIdentity)(contentAsString(_) must include(MOCKED_MESSAGE))
    }
  }

  "GET failedIV" should {
    "display the Failed IV error page" in new Setup {
      callAuthorised(testController.failedIV)(contentAsString(_) must include(MOCKED_MESSAGE))
    }
  }

  "GET lockedOut" should {
    "display the Locked out error page" in new Setup {
      callAuthorised(testController.lockedOut)(contentAsString(_) must include(MOCKED_MESSAGE))
    }
  }

  "GET userAborted" should {
    "display the User aborted error page" in new Setup {
      callAuthorised(testController.userAborted)(contentAsString(_) must include(MOCKED_MESSAGE))
    }
  }
  "GET failedIVJourney" should {
    "redirect the user to the correct page based on the result" in new Setup {
      when(mockIVService.fetchAndSaveIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.FailedIV))

      callAuthorised(testController.failedIVJourney("123")) {
        redirectLocation(_) mustBe Some(features.officer.controllers.routes.IdentityVerificationController.failedIV().url)
      }
    }
    "should NOT redirect user to a failed page if the response from vat backend is not a 200" in new Setup {
      when(mockIVService.fetchAndSaveIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse("Error occurred while updating IV Status", 500, 500)))

      callAuthorised(testController.failedIVJourney("123")) { res =>
        redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
        status(res) mustBe 303
      }
    }
  }
}
