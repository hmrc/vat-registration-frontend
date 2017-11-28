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

package features.iv.controllers

import java.util.concurrent.TimeUnit

import connectors.KeystoreConnector
import controllers.iv.IdentityVerificationController
import helpers.VatRegSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import akka.util.Timeout
import common.enums.{IVResult, VatRegStatus}
import models.CurrentProfile

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class IdentityVerificationControllerSpec extends VatRegSpec {
class Setup {
  val testController = new IdentityVerificationController(
    ds,
    mockIdentityVerificationConnector,
    mockIVService,
    mockCurrentProfile,
    mockAuthConnector,
    mockKeystoreConnector
  )

}
  "redirectToIV" should {
    "redirect to link returned from IV Proxy" in new Setup {
      mockGetCurrentProfile()

      when(mockIVService.setupAndGetIVJourneyURL(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful("/test-redirect-uri"))

      callAuthorised(testController.redirectToIV()) { res =>
        redirectLocation(res)(Timeout(FiniteDuration(5, TimeUnit.SECONDS))) mustBe Some("/test-redirect-uri")
      }
    }
    "throw error if no link is returned" in new Setup {
      mockGetCurrentProfile()

      when(mockIVService.setupAndGetIVJourneyURL(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception))

      callAuthorised(testController.redirectToIV()) { res =>
        redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
        status(res) mustBe 303
      }
    }
  }

  "completedIVJourney" should {
    "return 303 when the user has a journeyID inS4l and outcome is success" in new Setup {
      mockGetCurrentProfile()
      when(mockIVService.getJourneyIdAndJourneyOutcome()(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(IVResult.Success))
      when(mockIVService.setIvStatus(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(Some(IVResult.Success)))
      when(mockCurrentProfile.updateIVStatusInCurrentProfile(ArgumentMatchers.any())
      (ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(CurrentProfile("","","",VatRegStatus.draft,None,true)))
      callAuthorised(testController.completedIVJourney)(redirectLocation(_) mustBe Some(controllers.vatLodgingOfficer.routes.FormerNameController.show().url))
    }
    "return 500 when the user does not have journeyID/response from iv" in new Setup {
      mockGetCurrentProfile()
      when(mockIVService.getJourneyIdAndJourneyOutcome()(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(IVResult.Timeout))
      when(mockIVService.setIvStatus(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(None))

      callAuthorised(testController.completedIVJourney)(res => {
        redirectLocation(res) mustBe None
        status(res) mustBe 500
      }
      )
    }
  }
  "GET timeoutIV" should {
    "display the timeout IV error page" in new Setup {
      mockGetCurrentProfile()

      callAuthorised(testController.timeoutIV)(_ includesText "Your session has timed out due to inactivity")
    }
  }

  "GET unableToConfirmIdentity" should {
    "display the Unable to confirme identity error page" in new Setup{
      mockGetCurrentProfile()

      callAuthorised(testController.unableToConfirmIdentity)(_ includesText "We can&#x27;t confirm your identity")
    }
  }

  "GET failedIV" should {
    "display the Failed IV error page" in new Setup{
      mockGetCurrentProfile()

      callAuthorised(testController.failedIV)(_ includesText "We couldn&#x27;t confirm your identity")
    }
  }

  "GET lockedOut" should {
    "display the Locked out error page" in new Setup{
      mockGetCurrentProfile()

      callAuthorised(testController.lockedOut)(_ includesText "You&#x27;ve been locked out")
    }
  }

  "GET userAborted" should {
    "display the User aborted error page" in new Setup{
      mockGetCurrentProfile()

      callAuthorised(testController.userAborted)(_ includesText "You haven&#x27;t completed the identity check")
    }
  }
  "GET failedIVJourney" should {
    "redirect the user to the correct page based on the result" in new Setup {
      mockGetCurrentProfile()
      when(mockIVService.getJourneyIdAndJourneyOutcome()(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(IVResult.FailedIV))
      when(mockIVService.setIvStatus(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(Some(IVResult.FailedIV)))

      callAuthorised(testController.failedIVJourney("123"))(redirectLocation(_) mustBe Some(controllers.iv.routes.IdentityVerificationController.failedIV().url))
    }
    "should NOT redirect user to a failed page if the response from vat backend is not a 200" in new Setup {
      mockGetCurrentProfile()
      when(mockIVService.getJourneyIdAndJourneyOutcome()(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(IVResult.FailedIV))
      when(mockIVService.setIvStatus(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(None))

      callAuthorised(testController.failedIVJourney("123"))(res => {
        redirectLocation(res) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
        status(res) mustBe 303
      }
      )
    }
  }
}
