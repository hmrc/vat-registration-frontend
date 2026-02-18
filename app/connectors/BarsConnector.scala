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

package connectors

import config.FrontendAppConfig
import models.bars.{BarsVerificationResponse, UpstreamBarsException}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class BarsConnector @Inject() (
                                     config: FrontendAppConfig,
                                     http: HttpClientV2
                                   )(implicit ec: ExecutionContext)
  extends HttpReadsInstances
    with Logging {

  def verify(endpoint: String, requestJson: JsValue)(implicit hc: HeaderCarrier): Future[BarsVerificationResponse] = {
    val url = s"${config.verifyBankDetailsUrl(endpoint)}"

    http
      .post(url"$url")
      .withBody(requestJson)
      .execute[Either[UpstreamErrorResponse, BarsVerificationResponse]]
      .flatMap {
        case Right(verificationData) => Future.successful(verificationData)

        case Left(errorResponse) =>
          logger.warn(s"BARS verification failed with UpstreamErrorResponse: $errorResponse")

          // Safely extract JSON from message string
          val errorCode: Option[String] = {
            val pattern = "\\{.*\\}".r
            pattern.findFirstIn(errorResponse.message).flatMap { jsonStr =>
              try {
                (Json.parse(jsonStr) \ "code").asOpt[String]
              } catch {
                case _: Exception => None
              }
            }
          }

          Future.failed(
            UpstreamBarsException(
              status     = errorResponse.statusCode,
              errorCode  = errorCode,
              rawMessage = errorResponse.message
            )
          )
      }
  }

}
