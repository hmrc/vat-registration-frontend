/*
 * Copyright 2022 HM Revenue & Customs
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
import models.PartnerEntity
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnersConnector @Inject()(httpClient: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def getAllPartners(regId: String)(implicit hc: HeaderCarrier): Future[List[PartnerEntity]] = {
    implicit val readRaw: HttpReads[HttpResponse] = HttpReads.Implicits.readRaw

    httpClient.GET[HttpResponse](config.partnersApiUrl(regId)).map { result =>
      result.status match {
        case OK => result.json.validate[List[PartnerEntity]]
          .getOrElse(throw new InternalServerException("[PartnersConnector][getAllPartners] returned OK but partner list could not be parsed"))
        case NOT_FOUND => Nil
        case status => throw new InternalServerException(s"[PartnersConnector][getAllPartners] unexpected status from backend: $status")
      }
    }
  }

  def getPartner(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Option[PartnerEntity]] = {
    implicit val readRaw: HttpReads[HttpResponse] = HttpReads.Implicits.readRaw

    httpClient.GET[HttpResponse](s"${config.partnersApiUrl(regId)}/$index").map { result =>
      result.status match {
        case OK => result.json.validateOpt[PartnerEntity]
          .getOrElse(throw new InternalServerException("[PartnersConnector][getPartner] returned OK but partner could not be parsed"))
        case NOT_FOUND => None
        case status => throw new InternalServerException(s"[PartnersConnector][getPartner] unexpected status from backend: $status")
      }
    }
  }

  def upsertPartner(regId: String, index: Int, partner: PartnerEntity)(implicit hc: HeaderCarrier): Future[PartnerEntity] =
    httpClient.PUT(s"${config.partnersApiUrl(regId)}/$index", partner).map { result =>
      result.status match {
        case CREATED => result.json.validate[PartnerEntity]
          .getOrElse(throw new InternalServerException("[PartnersConnector][upsertPartner] returned CREATED but partner could not be parsed"))
        case status => throw new InternalServerException(s"[PartnersConnector][upsertPartner] unexpected status from backend: $status")
      }

    }

  def deletePartner(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Boolean] = {
    httpClient.DELETE[HttpResponse](s"${config.partnersApiUrl(regId)}/$index").map {
      _.status match {
        case NO_CONTENT => true
        case status => throw new InternalServerException(s"[PartnersConnector][deletePartner] unexpected status from backend: $status")
      }
    }
  }
}
