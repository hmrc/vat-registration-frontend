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

package connectors

import javax.inject.Inject

import config.WSHttp
import play.api.Logger
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class ICLConnectorImpl @Inject()(val http: WSHttp, config: ServicesConfig) extends ICLConnector {
  val baseUri = config.getConfString("industry-classification-lookup-frontend.uri",
    throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.uri'"))
  val iclFEurl: String = config.baseUrl("industry-classification-lookup-frontend") + baseUri
  val IClFEinternal: String = config.baseUrl("industry-classification-lookup-frontend-internal")
  val initialiseJourney = config.getConfString("industry-classification-lookup-frontend.initialise-journey", throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.initialise-journey'"))

  val IClInitialiseUrl = iclFEurl + initialiseJourney
}

trait ICLConnector {
  val http: WSHttp
  val IClInitialiseUrl: String
  val IClFEinternal: String

  def iclSetup(js:JsObject)(implicit hc : HeaderCarrier): Future[JsObject] = {
    http.POST[JsObject, HttpResponse](IClInitialiseUrl, js)
      .map(_.json.as[JsObject])
      .recover{
        case ex =>
          Logger.error(s"[ICLConnector] [ICLSetup] Threw an exception whilst Posting to initialise a new ICL journey with message: ${ex.getMessage}")
          throw ex
      }
  }

  def iclGetResult(fetchResultsUrl:String)(implicit hc:HeaderCarrier): Future[JsObject] = {
    http.GET[HttpResponse](IClFEinternal + fetchResultsUrl)
      .map (_.json.as[JsObject])
      .recover {
        case ex =>
          Logger.error(s"[ICLConnector] [ICLGetResult] Threw an exception while getting ICL journey results with message: ${ex.getMessage}")
            throw ex
        }
    }
  }