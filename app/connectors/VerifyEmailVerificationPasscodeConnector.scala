/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import connectors.VerifyEmailVerificationPasscodeParser._
import models.external._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object VerifyEmailVerificationPasscodeParser {

  val CodeKey = "code"
  val PasscodeMismatchKey = "PASSCODE_MISMATCH"
  val PasscodeNotFoundKey = "PASSCODE_NOT_FOUND"
  val MaxAttemptsExceededKey = "MAX_PASSCODE_ATTEMPTS_EXCEEDED"

  implicit object VerifyEmailVerificationPasscodeHttpReads extends HttpReads[VerifyEmailPasscodeResult] {
    override def read(method: String, url: String, response: HttpResponse): VerifyEmailPasscodeResult = {

      def errorCode: Option[String] = (response.json \ CodeKey).asOpt[String]

      response.status match {
        case CREATED =>
          infoConnectorLog("[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] email verified successfully")(response)
          EmailVerifiedSuccessfully
        case NO_CONTENT =>
          infoConnectorLog("[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] email already verified")(response)
          EmailAlreadyVerified
        case NOT_FOUND if errorCode contains PasscodeMismatchKey =>
          errorConnectorLog(s"[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] email not verified. NOT_FOUND $PasscodeMismatchKey ")(response)
          PasscodeMismatch
        case NOT_FOUND if errorCode contains PasscodeNotFoundKey =>
          errorConnectorLog(s"[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] email not verified. NOT_FOUND $PasscodeNotFoundKey ")(response)
          PasscodeNotFound
        case FORBIDDEN if errorCode contains MaxAttemptsExceededKey =>
          errorConnectorLog(s"[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] email not verified. FORBIDDEN $MaxAttemptsExceededKey ")(response)
          MaxAttemptsExceeded
        case status =>
          val logMsg = s"Unexpected response returned from VerifyEmailPasscode endpoint - Status: $status, response: ${response.body}"
          errorConnectorLog(s"[VerifyEmailVerificationPasscodeParser][VerifyEmailVerificationPasscodeHttpReads] $logMsg")(response)
          throw new InternalServerException(logMsg)
      }
    }
  }

}

@Singleton
class VerifyEmailVerificationPasscodeConnector @Inject()(httpClient: HttpClientV2,
                                                         config: FrontendAppConfig
                                                        )(implicit ec: ExecutionContext) {

  def verifyEmailVerificationPasscode(email: String, passcode: String)(implicit hc: HeaderCarrier): Future[VerifyEmailPasscodeResult] = {

    val url = config.verifyEmailVerificationPasscodeUrl()

    val jsonBody = Json.obj(
      "email" -> email,
      "passcode" -> passcode)

    httpClient.post(url"$url")
      .withBody(jsonBody)
      .execute[VerifyEmailPasscodeResult]
  }
}


