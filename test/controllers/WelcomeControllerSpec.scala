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

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import models.CurrentProfile
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions, MockMessages}

import scala.concurrent.Future

class WelcomeControllerSpec extends ControllerSpec with MockMessages with FutureAssertions with VatRegistrationFixture {

  val testController = new WelcomeController {
    override val currentProfileService = mockCurrentProfile
    override val keystoreConnector = mockKeystoreConnector
    override val vatRegistrationService = mockVatRegistrationService
    override val eligibilityFE: Call = Call("GET", "/test-url")
    val authConnector = mockAuthClientConnector
    val messagesApi: MessagesApi = mockMessagesAPI
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.WelcomeController.show())

  val testCurrentProfile = CurrentProfile("testRegid", VatRegStatus.draft)

  "GET /before-you-register-for-vat" should {
    "return HTML" when {
      "user is authorized to access" in {
        mockAllMessages
        mockAuthenticated()

        when(mockVatRegistrationService.createRegistrationFootprint(any()))
          .thenReturn(Future.successful(testRegId))
        when(mockCurrentProfile.buildCurrentProfile(any())(any()))
          .thenReturn(Future.successful(testCurrentProfile))

        when(mockVatRegistrationService.getTaxableThreshold(any())(any())) thenReturn Future.successful(formattedThreshold)

        callAuthorisedOrg(testController.start) {
          result =>
            status(result) mustBe OK
            contentType(result) mustBe Some("text/html")
            charset(result) mustBe Some("utf-8")
        }
      }
    }
  }

  "GET /" should {
    "redirect the user to start page" in {
      testController.show(fakeRequest) redirectsTo routes.WelcomeController.start().url
    }
  }

  "GET /redirect-to-eligibility" should {
    "redirect to eligibility front end" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.redirectToEligibility) {
        _ redirectsTo testController.eligibilityFE.url
      }
    }
  }
}
