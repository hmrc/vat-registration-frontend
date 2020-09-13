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

package mocks

import models.external.EmailVerificationResult
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import services.EmailVerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait EmailVerificationServiceMock {
  this: MockitoSugar =>

  val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]

  def mockRequestEmailVerificationPasscode(email: String)(response: Future[EmailVerificationResult]): Unit =
    when(mockEmailVerificationService.requestEmailVerificationPasscode(ArgumentMatchers.eq(email))(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)

  def mockVerifyEmailVerificationPasscode(passcode: String)(response: Future[EmailVerificationResult]): Unit =
    when(mockEmailVerificationService.verifyEmailVerificationPasscode(ArgumentMatchers.eq(passcode))(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)

}
