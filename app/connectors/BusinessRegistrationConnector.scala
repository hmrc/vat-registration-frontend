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

package connectors

import javax.inject.Inject

import config.WSHttp
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class BusinessRegistrationConnector @Inject()(val http: WSHttp,
                                              config: ServicesConfig) extends BusinessRegistrationConnect {
  val businessRegistrationUrl: String = config.baseUrl("business-registration")
  val businessRegistrationUri: String = config.getConfString("business-registration.uri",
    throw new RuntimeException("[BusinessRegistrationConnector] Could not retrieve config for 'business-registration.uri'"))
}

trait BusinessRegistrationConnect {

  val businessRegistrationUrl: String
  val businessRegistrationUri: String
  val http: WSHttp

  def getBusinessRegistrationID(implicit hc: HeaderCarrier): Future[Option[String]] = {
    http.GET[HttpResponse](s"$businessRegistrationUrl$businessRegistrationUri/business-tax-registration") map { response =>
      if (response.status == NOT_FOUND) {
        None
      } else {
        Some((response.json \ "registrationID").as[String])
      }
    } recover {
      case e => None
    }
  }
}
