/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NonRepudiationConnector @Inject()(httpClient: HttpClient)
                                       (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) {

  def storeEncodedUserAnswers(regId: String, encodedHtml: String)(implicit hc: HeaderCarrier): Future[StoreNrsPayloadSuccess.type] =
    httpClient.PATCH(
      url = appConfig.storeNrsPayloadUrl(regId),
      body = Json.obj("payload" -> encodedHtml)
    ) map {
      _.status match {
        case OK => StoreNrsPayloadSuccess
        case _ => throw new InternalServerException("[NonRepudiationConnecotr][storeEncodedUserAnswers] Failed to store user answers")
      }
    }

}

object NonRepudiationConnector {
  case object StoreNrsPayloadSuccess
}
