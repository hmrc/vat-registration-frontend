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
import featuretoggle.FeatureSwitch.StubIncorpIdJourney
import featuretoggle.FeatureToggleSupport
import models.api.{CharitableOrg, PartyType, RegSociety, UkCompany}
import models.external.IncorporatedEntity
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import play.api.http.Status.CREATED
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Request
import services.SessionService
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import utils.{LoggingUtil, SessionIdRequestHelper}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorpIdConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig, sessionService: SessionService)
                                 (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FeatureToggleSupport with LoggingUtil {

  def createJourney(journeyConfig: IncorpIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    val url = partyType match {
      case UkCompany => config.startUkCompanyIncorpJourneyUrl()
      case RegSociety => config.startRegSocietyIncorpIdJourneyUrl()
      case CharitableOrg => config.startCharitableOrgIncorpIdJourneyUrl()
      case _ => throw new InternalServerException(s"Party type $partyType is not a valid incorporated entity party type")
    }

    val incorpIdRequest = SessionIdRequestHelper.conditionallyAddSessionIdHeader(
      baseRequest = httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)),
      condition = isEnabled(StubIncorpIdJourney)
    )

    incorpIdRequest.execute.map {
      case response@HttpResponse(CREATED, _, _) =>
        val journeyStartUrl = (response.json \ "journeyStartUrl").as[String]
        infoLog(s"Incorp ID journey created for party type: $partyType")
        journeyStartUrl
      case response =>
        val errorMessage = s"Invalid response from incorporated entity identification for $partyType: Status: ${response.status} Body: ${response.body}"
        errorLog(errorMessage)
        throw new InternalServerException(errorMessage)
    }
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[IncorporatedEntity] = {
    val url = config.getIncorpIdDetailsUrl(journeyId)

    val incorpIdRequest = SessionIdRequestHelper.conditionallyAddSessionIdHeader(
      baseRequest = httpClient.get(url"$url"),
      condition = isEnabled(StubIncorpIdJourney)
    )

    incorpIdRequest
      .execute
      .map(res => {
        IncorporatedEntity.apiFormat.reads(res.json) match {
          case JsSuccess(value, _) =>
            infoLog(s"Retrieved details for Incorp ID journey: $journeyId")
            value
          case JsError(errors) =>
            val errorMessage = s"Incorp ID returned invalid JSON ${errors.map(_._1).mkString(", ")}"
            errorLog(errorMessage)
            throw new Exception(errorMessage)
        }
      })
  }
}
