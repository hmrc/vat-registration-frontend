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

package connectors.test

import connectors._
import models.CurrentProfile
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestVatRegistrationConnector @Inject()(val http: HttpClient, config: ServicesConfig)
                                            (implicit ec: ExecutionContext) {

  val vatRegUrl: String = config.baseUrl("vat-registration")

  def setupCurrentProfile(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty[HttpResponse](s"$vatRegUrl/vatreg/test-only/current-profile-setup").map(_ => Results.Ok)
  }

  def updateEligibilityData(jsonData: JsValue)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[HttpResponse] = {
    http.PATCH[JsValue, HttpResponse](s"$vatRegUrl/vatreg/${currentProfile.registrationId}/eligibility-data", jsonData) recover {
      case e: Exception => throw logResponse(e, "updateEligibilityData")
    }
  }

  def updateTrafficManagementQuota(partyType: String, isEnrolled: Boolean, newQuota: Int)(implicit hc: HeaderCarrier) =
    http.PUT(
      url = s"$vatRegUrl/vatreg/test-only/api/daily-quota",
      body = Json.obj(
        "partyType" -> partyType,
        "isEnrolled" -> isEnrolled,
        "quota" -> newQuota
      )
    ) recover {
      case e: Exception => throw logResponse(e, "updateTrafficManagementQuota")
    }

  def clearTrafficManagement(implicit hc: HeaderCarrier) =
    http.DELETE(s"$vatRegUrl/vatreg/test-only/api/traffic-management") recover {
      case e: Exception => throw logResponse(e, "updateTrafficManagementQuota")
    }

  def retrieveVatSubmission(regId: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET(s"$vatRegUrl/vatreg/test-only/submissions/$regId/submission-payload") map (_.json)
  }



}
