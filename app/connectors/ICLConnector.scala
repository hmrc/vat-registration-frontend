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

import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import utils.LoggingUtil
import play.api.mvc.Request

@Singleton
class ICLConnector @Inject()(val http: HttpClientV2, config: ServicesConfig)
                            (implicit ec: ExecutionContext) extends LoggingUtil {

  val baseUri: String = config.getConfString("industry-classification-lookup-frontend.uri",
    throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.uri'"))
  val iclFEurl: String = config.baseUrl("industry-classification-lookup-frontend") + baseUri
  val IClFEinternal: String = config.baseUrl("industry-classification-lookup-frontend-internal")
  val initialiseJourney: String = config.getConfString("industry-classification-lookup-frontend.initialise-journey", throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.initialise-journey'"))

  val IClInitialiseUrl: String = iclFEurl + initialiseJourney

  def iclSetup(js: JsObject)(implicit hc: HeaderCarrier, request: Request[_]): Future[JsValue] = {
    infoLog("[iclSetup] Initializing a new ICL journey")
    http.post(url"$IClInitialiseUrl")
      .withBody(js)
      .execute[JsValue]
      .recover {
        case ex =>
          val errorMessage = s"[ICLConnector] [iclSetup] Threw an exception whilst Posting to initialise a new ICL journey with message: ${ex.getMessage}"
          errorLog(errorMessage)
          throw ex
      }
  }

  def iclGetResult(fetchResultsUrl: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[JsValue] = {
    infoLog("[iclGetResult] Getting ICL journey results")
    http.get(url"${IClFEinternal + fetchResultsUrl}")
      .execute[JsValue]
      .recover {
        case ex =>
          val errorMessage = s"[ICLConnector] [iclGetResult] Threw an exception while getting ICL journey results with message: ${ex.getMessage}"
          errorLog(errorMessage)
          throw ex
      }
  }
}
