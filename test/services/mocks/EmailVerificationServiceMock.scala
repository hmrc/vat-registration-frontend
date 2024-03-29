/*
 * Copyright 2024 HM Revenue & Customs
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

package services.mocks

import models.external.{RequestEmailPasscodeResult, VerifyEmailPasscodeResult}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import services.EmailVerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait EmailVerificationServiceMock {
  this: MockitoSugar =>

  val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]

  def mockRequestEmailVerificationPasscode(email: String)(response: Future[RequestEmailPasscodeResult]): Unit =
    when(mockEmailVerificationService.requestEmailVerificationPasscode(ArgumentMatchers.eq(email))(ArgumentMatchers.any[HeaderCarrier], any[Request[_]]))
      .thenReturn(response)

  def mockVerifyEmailVerificationPasscode(email: String, passcode: String)(response: Future[VerifyEmailPasscodeResult]): Unit =
    when(mockEmailVerificationService.verifyEmailVerificationPasscode(
      ArgumentMatchers.eq(email),
      ArgumentMatchers.eq(passcode)
    )(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)

}
