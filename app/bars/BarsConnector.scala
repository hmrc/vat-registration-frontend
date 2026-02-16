/*
 * Copyright 2026 HM Revenue & Customs
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

package bars

import bars.model.request.{BarsVerifyBusinessRequest, BarsVerifyPersonalRequest}
import bars.model.response.{BarsVerifyResponse, UpstreamBarsException}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import config.FrontendAppConfig
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Connects to the Bank Account Reputation Service to validate bank accounts.
 */
@Singleton
class BarsConnector @Inject()(
                               httpClient: HttpClientV2,
                               config: FrontendAppConfig
                             )(implicit ec: ExecutionContext) extends Logging {


  /** "This endpoint checks the likely correctness of a given personal bank account and it's likely connection to the
   * given account holder (aka the subject)"
   */

  def verifyPersonal(barsVerifyPersonalRequest: BarsVerifyPersonalRequest)(implicit hc: HeaderCarrier): Future[BarsVerifyResponse] =
    httpClient
      .post(url"${config.verifyBarsPersonalUrl}")
      .withBody(Json.toJson(barsVerifyPersonalRequest))
      .execute[BarsVerifyResponse]
      .recoverWith { case error: Exception =>
        val (status, message) = error match {
          case u: UpstreamErrorResponse => (u.statusCode, u.message)
          case h: HttpException         => (h.responseCode, h.message)
          case _                        => (Status.INTERNAL_SERVER_ERROR, Option(error.getMessage).getOrElse(error.toString))
        }

        logger.warn(s"BARS personal endpoint verification failed: $status - $message", error)

        Future.failed(
          UpstreamBarsException(
            status     = status,
            errorCode  = None,
            rawMessage = message
          )
        )
      }

  /** "This endpoint checks the likely correctness of a given business bank account and it's likely connection to the
   * given business"
   */
  def verifyBusiness(barsVerifyBusinessRequest: BarsVerifyBusinessRequest)(implicit hc: HeaderCarrier): Future[BarsVerifyResponse] = {
    httpClient
      .post(url"${config.verifyBarsBusinessUrl}")
      .withBody(Json.toJson(barsVerifyBusinessRequest))
      .execute[BarsVerifyResponse]
      .recoverWith {
        case error: Exception =>
          val (status, message) = error match {
            case u: UpstreamErrorResponse => (u.statusCode, u.message)
            case h: HttpException         => (h.responseCode, h.message)
            case _                        => (Status.INTERNAL_SERVER_ERROR, Option(error.getMessage).getOrElse(error.toString))
          }

          logger.warn(s"BARS business endpoint verification failed: $status - $message", error)

          Future.failed(
            UpstreamBarsException(
              status     = status,
              errorCode  = None,
              rawMessage = message
            )
          )
      }

  }
}

