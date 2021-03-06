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

import javax.inject.{Inject, Singleton}
import models.api.trafficmanagement.{ClearTrafficManagementError, ClearTrafficManagementResponse, RegistrationInformation, TrafficManagementCleared}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

@Singleton
class TrafficManagementConnector @Inject()(val httpClient: HttpClient,
                                           appConfig: FrontendAppConfig
                                          )(implicit executionContext: ExecutionContext) {

  def getRegistrationInformation(implicit hc: HeaderCarrier): Future[Option[RegistrationInformation]] =
    httpClient.GET[Option[RegistrationInformation]](appConfig.getRegistrationInformationUrl)

  def clearTrafficManagement(implicit hc: HeaderCarrier): Future[ClearTrafficManagementResponse] =
    httpClient.DELETE(appConfig.clearTrafficManagementUrl) map {
      _.status match {
        case NO_CONTENT => TrafficManagementCleared
        case status => ClearTrafficManagementError(status)
      }
    }

}
