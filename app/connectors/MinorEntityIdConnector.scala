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
import models.api.{NonUkNonEstablished, PartyType, Trust, UnincorpAssoc}
import models.external.MinorEntity
import models.external.minorentityid.MinorEntityIdJourneyConfig
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import utils.LoggingUtil
import play.api.mvc.Request

@Singleton
class MinorEntityIdConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig)
                                      (implicit ec: ExecutionContext) extends LoggingUtil {

  def createJourney(journeyConfig: MinorEntityIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    val url = partyType match {
      case UnincorpAssoc => config.startUnincorpAssocJourneyUrl
      case Trust => config.startTrustJourneyUrl
      case NonUkNonEstablished => config.startNonUKCompanyJourneyUrl
      case _ => throw new InternalServerException(s"Party type $partyType is not a valid minor entity party type")
    }

    httpClient.post(url"$url")
      .withBody(Json.toJson(journeyConfig))
      .execute.map {
        case response@HttpResponse(CREATED, _, _) =>
          val journeyStartUrl = (response.json \ "journeyStartUrl").as[String]
          infoLog(s"Minor Entity ID journey created for party type: $partyType")
          journeyStartUrl
        case response =>
          val errorMessage = s"[MinorEntityIdConnector] Invalid response from minor entity identification: Status: ${response.status} Body: ${response.body}"
          errorLog(errorMessage)
          throw new InternalServerException(errorMessage)
      }
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[MinorEntity] =
    httpClient.get(url"${config.getMinorEntityIdDetailsUrl(journeyId)}")
      .execute
      .map { response =>
        response.status match {
          case OK => response.json.validate[MinorEntity](MinorEntity.apiFormat) match {
            case JsSuccess(value, _) =>
              infoLog(s"Retrieved details for Minor Entity ID journey: $journeyId")
              value
            case JsError(errors) =>
              val errorMessage = s"[MinorEntityIdConnector] Minor Entity ID returned invalid JSON ${errors.map(_._1).mkString(", ")}"
              errorLog(errorMessage)
              throw new InternalServerException(errorMessage)
          }
          case status =>
            val errorMessage = s"[MinorEntityIdConnector] Unexpected status returned from minor entity ID: $status"
            errorLog(errorMessage)
            throw new InternalServerException(errorMessage)
        }
      }
}
