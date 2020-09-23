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

package connectors

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.external.{EmailAlreadyVerified, EmailVerifiedSuccessfully, PasscodeNotFound, VerifyEmailPasscodeResult}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailVerificationPasscodeConnector @Inject()(httpClient: HttpClient,
                                                         config: FrontendAppConfig
                                                        )(implicit ec: ExecutionContext) {

  def verifyEmailVerificationPasscode(email: String, passcode: String)(implicit hc: HeaderCarrier): Future[VerifyEmailPasscodeResult] = {

    val url = config.verifyEmailVerificationPasscodeUrl()

    val jsonBody = Json.obj(
      "email" -> email,
      "passcode" -> passcode)

    httpClient.POST(url, jsonBody).map {
      case HttpResponse(CREATED, _, _) => EmailVerifiedSuccessfully
      case HttpResponse(NO_CONTENT, _, _) => EmailAlreadyVerified
    }.recover {
      case _:NotFoundException => PasscodeNotFound
    }
  }

}
