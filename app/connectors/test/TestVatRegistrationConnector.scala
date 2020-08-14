/*
 * Copyright 2020 HM Revenue & Customs
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

import config.WSHttp
import connectors._
import javax.inject.{Inject, Singleton}
import models.CurrentProfile
import play.api.libs.json.JsValue
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class TestVatRegistrationConnector @Inject()(val http: WSHttp, config: ServicesConfig) {
  val vatRegUrl: String = config.baseUrl("vat-registration")

  val incorporationFrontendStubsUrl: String = config.baseUrl("incorporation-frontend-stub")
  val incorporationFrontendStubsUri: String = config.getConfString("incorporation-frontend-stub.uri", "")

  def setupCurrentProfile(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty[HttpResponse](s"$vatRegUrl/vatreg/test-only/current-profile-setup").map(_ => Results.Ok)
  }

  def updateEligibilityData(jsonData: JsValue)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[HttpResponse] = {
    http.PATCH[JsValue, HttpResponse](s"$vatRegUrl/vatreg/${currentProfile.registrationId}/eligibility-data", jsonData) recover {
      case e: Exception => throw logResponse(e, "updateEligibilityData")
    }
  }
}
