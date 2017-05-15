/*
 * Copyright 2017 HM Revenue & Customs
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
import com.google.inject.ImplementedBy
import config.WSHttp
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[TestVatRegistrationConnector])
trait TestRegistrationConnector {
  def setupCurrentProfile()(implicit hc: HeaderCarrier): Future[Result]
  def dropCollection()(implicit hc: HeaderCarrier): Future[Result]
  def postTestData(jsonData: JsValue)(implicit hc : HeaderCarrier) : Future[HttpResponse]
  def wipeTestData()(implicit hc : HeaderCarrier) : Future[HttpResponse]
}

class TestVatRegistrationConnector extends TestRegistrationConnector with ServicesConfig {

  //$COVERAGE-OFF$
  val vatRegUrl = baseUrl("vat-registration")
  val http = WSHttp

  lazy val incorporationFrontendStubsUrl: String = baseUrl("incorporation-frontend-stub")
  lazy val incorporationFrontendStubsUri: String = getConfString("incorporation-frontend-stub.uri","")

  def setupCurrentProfile()(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty(s"$vatRegUrl/vatreg/test-only/current-profile-setup").map { _ => Results.Ok }
  }

  def dropCollection()(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty(s"$vatRegUrl/vatreg/test-only/clear").map { _ => Results.Ok }
  }

  def postTestData(jsonData: JsValue)(implicit hc : HeaderCarrier) : Future[HttpResponse] = {
      Logger.debug(s"###111###$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/insert-data")
      http.POST[JsValue, HttpResponse](s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/insert-data", jsonData) recover {
        case e: Exception => throw logResponse(e,"TestVatRegistrationConnector", s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/insert-data")
      }
  }

  def wipeTestData()(implicit hc : HeaderCarrier) :Future[HttpResponse] = {
    http.PUT[JsValue, HttpResponse](s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/wipe-data", Json.parse("{}")) recover {
      case e: Exception => throw logResponse(e,"TestVatRegistrationConnector", s"$incorporationFrontendStubsUrl$incorporationFrontendStubsUri/wipe-data")
    }
  }

  //$COVERAGE-ON$

}
