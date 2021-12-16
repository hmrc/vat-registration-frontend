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
import models.api.{Partnership, PartyType, Trust}
import models.external.PartnershipIdEntity
import models.external.partnershipid.PartnershipIdJourneyConfig
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnershipIdConnector @Inject()(httpClient: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def createJourney(journeyConfig: PartnershipIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier): Future[String] = {
    val url = config.startPartnershipJourneyUrl(partyType)

    httpClient.POST(url, journeyConfig).map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(s"[PartnershipIdConnector] Invalid response from partnership identification: Status: ${response.status} Body: ${response.body}")
    }
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[PartnershipIdEntity] = {
    httpClient.GET(config.getPartnershipIdDetailsUrl(journeyId)).map { response =>
      response.status match {
        case OK => response.json.validate[PartnershipIdEntity](PartnershipIdEntity.apiFormat) match {
          case JsSuccess(value, _) => value
          case JsError(errors) =>
            throw new InternalServerException(s"[PartnershipIdConnector] Partnership ID returned invalid JSON ${errors.map(_._1).mkString(", ")}")
        }
        case status =>
          throw new InternalServerException(s"[PartnershipIdConnector] Unexpected status returned from partnership id: $status")
      }
    }
  }

}
