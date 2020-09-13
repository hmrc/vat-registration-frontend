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

import connectors.{RequestEmailVerificationPasscodeConnector, VerifyEmailVerificationPasscodeConnector}
import javax.inject.{Inject, Singleton}
import models.external.EmailVerificationResult
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class EmailVerificationService @Inject()(requestPasscodeConnector: RequestEmailVerificationPasscodeConnector,
                                         verifyPasscodeConnector: VerifyEmailVerificationPasscodeConnector) {

  def requestEmailVerificationPasscode(email: String)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] =
    requestPasscodeConnector.requestEmailVerificationPasscode(email)

  def verifyEmailVerificationPasscode(passcode: String)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] =
    verifyPasscodeConnector.verifyEmailVerificationPasscode(passcode)

}
