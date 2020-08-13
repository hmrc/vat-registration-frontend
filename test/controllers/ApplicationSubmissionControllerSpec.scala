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

import fixtures.VatRegistrationFixture
import models.Returns
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions, MockMessages}

import scala.concurrent.Future

class ApplicationSubmissionControllerSpec extends ControllerSpec with MockMessages with FutureAssertions with VatRegistrationFixture {

  val testController = new ApplicationSubmissionController(
    mockVatRegistrationService,
    mockReturnsService,
    mockAuthClientConnector,
    mockKeystoreConnector,
    mockMessagesAPI,
    mockServicesConfig
  )

  val fakeRequest = FakeRequest(routes.ApplicationSubmissionController.show())

  s"GET ${routes.ApplicationSubmissionController.show()}" should {
    "display the submission confirmation page to the user" in {
      mockAllMessages

      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockVatRegistrationService.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("testAckRef"))

      when(mockReturnsService.getReturns(any(), any(), any()))
        .thenReturn(Future.successful(Returns(None, None, None, None)))

      callAuthorised(testController.show) { res =>
        res includesText MOCKED_MESSAGE
      }
    }
  }

}
