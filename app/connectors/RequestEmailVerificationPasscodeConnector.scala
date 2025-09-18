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

package connectors

import config.FrontendAppConfig
import models.external.{AlreadyVerifiedEmailAddress, MaxEmailsExceeded, RequestEmailPasscodeResult, RequestEmailPasscodeSuccessful}
import play.api.http.Status.{CONFLICT, CREATED, FORBIDDEN}
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RequestEmailVerificationPasscodeConnector @Inject()(httpClient: HttpClientV2,
                                                          config: FrontendAppConfig
                                                         )(implicit ec: ExecutionContext) extends LoggingUtil {

  def requestEmailVerificationPasscode(email: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[RequestEmailPasscodeResult] = {

    val url = config.requestEmailVerificationPasscodeUrl()

    val jsonBody = Json.obj("email" -> email, "serviceName" -> "VAT Registration", "lang" -> "en")

    httpClient.post(url"$url")
      .withBody(jsonBody)
      .execute
      .map {
        case HttpResponse(CREATED, _, _) => RequestEmailPasscodeSuccessful
      }.recover {
        case error @ UpstreamErrorResponse(_, CONFLICT, _, _) if UpstreamErrorResponse.Upstream4xxResponse.unapply(error).isDefined =>
          infoLog(s"[RequestEmailVerificationPasscodeConnector][requestEmailVerificationPasscode] email already verified")
          AlreadyVerifiedEmailAddress
        case error @ UpstreamErrorResponse(_, FORBIDDEN, _, _) if UpstreamErrorResponse.Upstream4xxResponse.unapply(error).isDefined =>
          warnLog(s"[RequestEmailVerificationPasscodeConnector][requestEmailVerificationPasscode] max emails exceeded")
          MaxEmailsExceeded
        case error =>
          errorLog(s"[RequestEmailVerificationPasscodeConnector][requestEmailVerificationPasscode] unknown error. Msg: ${error.getMessage}")
          throw error
      }
  }

}
