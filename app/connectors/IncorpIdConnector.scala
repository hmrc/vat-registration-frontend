/*
 * Copyright 2023 HM Revenue & Customs
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
import featureswitch.core.config.{FeatureSwitching, StubIncorpIdJourney}
import models.api.{CharitableOrg, PartyType, RegSociety, UkCompany}
import models.external.IncorporatedEntity
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import play.api.http.Status.CREATED
import play.api.libs.json.{JsError, JsSuccess, Json}
import services.SessionService
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import utils.SessionIdRequestHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorpIdConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig, sessionService: SessionService)
                                 (implicit ec: ExecutionContext) extends FeatureSwitching {

  def createJourney(journeyConfig: IncorpIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier): Future[String] = {
    val url = partyType match {
      case UkCompany => config.startUkCompanyIncorpJourneyUrl()
      case RegSociety => config.startRegSocietyIncorpIdJourneyUrl()
      case CharitableOrg => config.startCharitableOrgIncorpIdJourneyUrl()
      case _ => throw new InternalServerException(s"Party type $partyType is not a valid incorporated entity party type")
    }

    val incorpIdRequewt = SessionIdRequestHelper.conditionallyAddSessionIdHeader(
      baseRequest = httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)),
      condition = isEnabled(StubIncorpIdJourney)
    )

    incorpIdRequewt.execute.map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(s"Invalid response from incorporated entity identification for $partyType: Status: ${response.status} Body: ${response.body}")
    }
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[IncorporatedEntity] = {
    val url = config.getIncorpIdDetailsUrl(journeyId)

    val incorpIdRequest = SessionIdRequestHelper.conditionallyAddSessionIdHeader(
      baseRequest = httpClient.get(url"$url"),
      condition = isEnabled(StubIncorpIdJourney)
    )

    incorpIdRequest
      .execute
      .map(res => {
        IncorporatedEntity.apiFormat.reads(res.json) match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw new Exception(s"Incorp ID returned invalid JSON ${errors.map(_._1).mkString(", ")}")
        }
      })
  }

}
