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
import models.TransactorDetails
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
  private val personalDetailsKey = "personalDetails"

  def startJourney(config: SoleTraderIdJourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    httpClient.POST(appConfig.soleTraderIdentificationJourneyUrl, Json.toJson(config)) map { response =>
      Logger.info("url " + response.body)
      response.status match {
        case CREATED => (response.json \  journeyUrlKey).validate[String] match {
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


  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[TransactorDetails] =
    httpClient.GET(appConfig.getRetrieveSoleTraderIdentificationResultUrl(journeyId)) map { response =>
      response.status match {
        case OK =>
          (response.json \ personalDetailsKey).validate[TransactorDetails] match {
            case JsSuccess(transactorDetails, _) =>
              transactorDetails
            case JsError(errors) =>
              throw new InternalServerException(s"Sole trader ID returned invalid JSON ${errors.map(_._1).mkString(", ")}")
          }
        case status =>
          throw new InternalServerException(s"[SoleTraderIdentificationConnector] Unexpected status returned from STI when retrieving sole trader details: ${status}")
      }
    }

}