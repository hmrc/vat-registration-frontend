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

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import models.IVResult
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import testHelpers.{ControllerSpec}
import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

class IdentityVerificationControllerSpec extends ControllerSpec {

  trait Setup {
    val testController: IdentityVerificationController = new IdentityVerificationController(
      messagesControllerComponents,
      mockIVService,
      mockAuthClientConnector,
      mockKeystoreConnector
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
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
        redirectLocation(_) mustBe Some(controllers.routes.OfficerController.showFormerName().url)
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
      callAuthorised(testController.timeoutIV)(status(_) mustBe OK)
    }
  }

  "GET unableToConfirmIdentity" should {
    "display the Unable to confirme identity error page" in new Setup {
      callAuthorised(testController.unableToConfirmIdentity)(status(_) mustBe OK)
    }
  }

  "GET failedIV" should {
    "display the Failed IV error page" in new Setup {
      callAuthorised(testController.failedIV)(status(_) mustBe OK)
    }
  }

  "GET lockedOut" should {
    "display the Locked out error page" in new Setup {
      callAuthorised(testController.lockedOut)(status(_) mustBe OK)
    }
  }

  "GET userAborted" should {
    "display the User aborted error page" in new Setup {
      callAuthorised(testController.userAborted)(status(_) mustBe OK)
    }
  }
  "GET failedIVJourney" should {
    "redirect the user to the correct page based on the result" in new Setup {
      when(mockIVService.fetchAndSaveIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.FailedIV))

      callAuthorised(testController.failedIVJourney("123")) {
        redirectLocation(_) mustBe Some(controllers.routes.IdentityVerificationController.failedIV().url)
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
