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
import models.PersonalDetails
import models.api.PartyType
import models.external.SoleTraderIdEntity
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException, UnauthorizedException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationConnector @Inject()(val httpClient: HttpClient, appConfig: FrontendAppConfig)
                                                 (implicit executionContext: ExecutionContext) {

  private val journeyUrlKey = "journeyStartUrl"

  def startSoleTraderJourney(config: SoleTraderIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier): Future[String] = {
    httpClient.POST(appConfig.soleTraderJourneyUrl(partyType), Json.toJson(config)) map { response =>
      Logger.info("url " + response.body)
      response.status match {
        case CREATED => (response.json \ journeyUrlKey).validate[String] match {
          case JsSuccess(journeyId, _) => journeyId
          case _ =>
            throw new InternalServerException(s"[SoleTraderIdentificationConnector] STI Response JSON did not include $journeyUrlKey key")
        }
        case UNAUTHORIZED =>
          throw new UnauthorizedException(s"[SoleTraderIdentificationConnector] Failed to create new journey as user was unauthorised")
        case status =>
          throw new InternalServerException(s"[SoleTraderIdentificationConnector] Sole trader identification returned an unexpected status: $status")
      }
    }
  }

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[(PersonalDetails, SoleTraderIdEntity)] =
    httpClient.GET(appConfig.retrieveSoleTraderIdentificationResultUrl(journeyId)) map { response =>
      response.status match {
        case OK =>
          (response.json.validate[PersonalDetails](PersonalDetails.soleTraderIdentificationReads),
            response.json.validate[SoleTraderIdEntity](SoleTraderIdEntity.apiFormat)) match {
            case (JsSuccess(transactorDetails, _), JsSuccess(optSoleTrader, _)) =>
              (transactorDetails, optSoleTrader)
            case (JsError(errors), _) =>
              throw new InternalServerException(s"Sole trader ID returned invalid JSON ${errors.map(_._1).mkString(", ")}")
          }
        case status =>
          throw new InternalServerException(s"[SoleTraderIdentificationConnector] Unexpected status returned from STI when retrieving sole trader details: $status")
      }
    }

  def startIndividualJourney(config: SoleTraderIdJourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    httpClient.POST(appConfig.individualJourneyUrl, Json.toJson(config)) map { response =>
      Logger.info("url " + response.body)
      response.status match {
        case CREATED => (response.json \ journeyUrlKey).validate[String] match {
          case JsSuccess(journeyId, _) => journeyId
          case _ =>
            throw new InternalServerException(s"[SoleTraderIdentificationConnector] STI Response JSON did not include $journeyUrlKey key")
        }
        case UNAUTHORIZED =>
          throw new UnauthorizedException(s"[SoleTraderIdentificationConnector] Failed to create new journey as user was unauthorised")
        case status =>
          throw new InternalServerException(s"[SoleTraderIdentificationConnector] Sole trader identification returned an unexpected status: $status")
      }
    }
  }

  def retrieveIndividualDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[PersonalDetails] =
    httpClient.GET(appConfig.retrieveSoleTraderIdentificationResultUrl(journeyId)) map { response =>
      response.status match {
        case OK =>
          response.json.validate[PersonalDetails](PersonalDetails.soleTraderIdentificationReads) match {
            case JsSuccess(individual, _) =>
              individual
            case JsError(errors) =>
              throw new InternalServerException(s"Sole trader ID returned invalid JSON ${errors.map(_._1).mkString(", ")}")
          }
        case status =>
          throw new InternalServerException(s"[SoleTraderIdentificationConnector] Unexpected status returned from STI when retrieving individual details: $status")
      }
    }
}