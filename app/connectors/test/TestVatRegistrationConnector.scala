/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import config.WSHttp
import connectors._
import models.CurrentProfile
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class TestVatRegistrationConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends TestRegistrationConnector {
  val vatRegUrl = config.baseUrl("vat-registration")

  val incorporationFrontendStubsUrl = config.baseUrl("incorporation-frontend-stub")
  val incorporationFrontendStubsUri = config.getConfString("incorporation-frontend-stub.uri", "")
}

trait TestRegistrationConnector {
  val http: WSHttp

  val vatRegUrl: String
  val incorporationFrontendStubsUrl: String
  val incorporationFrontendStubsUri: String

  def setupCurrentProfile(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty[HttpResponse](s"$vatRegUrl/vatreg/test-only/current-profile-setup").map(_ => Results.Ok)
  }

  def incorpCompany(incorpDate: String)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/test-only/incorporation-information/incorp-company/${currentProfile.transactionId}/$incorpDate")
  }

  def postTestData(jsonData: JsValue)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/insert-data", jsonData) recover {
      case e: Exception => throw logResponse(e, "postTestData")
    }
  }

  def wipeTestData(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.PUT[JsValue, HttpResponse](s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/wipe-data", Json.parse("{}")) recover {
      case e: Exception => throw logResponse(e, "wipeTestData")
    }
  }

  def updateEligibilityData(jsonData: JsValue)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[HttpResponse] = {
    http.PATCH[JsValue, HttpResponse](s"$vatRegUrl/vatreg/${currentProfile.registrationId}/eligibility-data", jsonData) recover {
      case e: Exception => throw logResponse(e, "updateEligibilityData")
    }
  }
}
