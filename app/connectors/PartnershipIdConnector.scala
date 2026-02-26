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
import models.api.PartyType
import models.external.PartnershipIdEntity
import models.external.partnershipid.PartnershipIdJourneyConfig
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
class PartnershipIdConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig)
                                      (implicit ec: ExecutionContext) extends LoggingUtil {

  def createJourney(journeyConfig: PartnershipIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    val url = config.startPartnershipJourneyUrl(partyType)

    httpClient.post(url"$url")
      .withBody(Json.toJson(journeyConfig))
      .execute
      .map {
        case response@HttpResponse(CREATED, _, _) =>
          val journeyStartUrl = (response.json \ "journeyStartUrl").as[String]
          infoLog(s"Partnership ID journey created for party type: $partyType")
          journeyStartUrl
        case response =>
          val errorMessage = s"[PartnershipIdConnector] Invalid response from partnership identification: Status: ${response.status} Body: ${response.body}"
          errorLog(errorMessage)
          throw new InternalServerException(errorMessage)
      }
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[PartnershipIdEntity] =
    httpClient.get(url"${config.getPartnershipIdDetailsUrl(journeyId)}")
      .execute
      .map { response =>
        response.status match {
          case OK => response.json.validate[PartnershipIdEntity](PartnershipIdEntity.apiFormat) match {
            case JsSuccess(value, _) =>
              infoLog(s"Retrieved details for Partnership ID journey: $journeyId")
              value
            case JsError(errors) =>
              val errorMessage = s"[PartnershipIdConnector] Partnership ID returned invalid JSON ${errors.map(_._1).mkString(", ")}"
              errorLog(errorMessage)
              throw new InternalServerException(errorMessage)
          }
          case status =>
            val errorMessage = s"[PartnershipIdConnector] Unexpected status returned from partnership id: $status"
            errorLog(errorMessage)
            throw new InternalServerException(errorMessage)
        }
      }
}
