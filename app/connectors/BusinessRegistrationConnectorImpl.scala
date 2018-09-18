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

import config.WSHttp
import features.tradingDetails.TradingDetails
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class BusinessRegistrationConnectorImpl @Inject()(val http: WSHttp,
                                                  config: ServicesConfig) extends BusinessRegistrationConnector {
  val businessRegistrationUrl: String = config.baseUrl("business-registration")
  val businessRegistrationUri: String = config.getConfString("business-registration.uri",
    throw new RuntimeException("[BusinessRegistrationConnector] Could not retrieve config for 'business-registration.uri'"))
}

trait BusinessRegistrationConnector {

  val businessRegistrationUrl: String
  val businessRegistrationUri: String
  val http: WSHttp

  def getBusinessRegistrationID(implicit hc: HeaderCarrier): Future[Option[String]] = {
    http.GET[HttpResponse](s"$businessRegistrationUrl$businessRegistrationUri/business-tax-registration") map { response =>
        Some((response.json \ "registrationID").as[String])
    } recover {
      case e =>
        Logger.warn(s"[BusinessRegistration][getBusinessRegistrationID] and error has occurred with message ${e.getMessage}")
        None
    }
  }

  def retrieveTradingName(regId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    http.GET[JsValue](s"$businessRegistrationUrl$businessRegistrationUri/$regId/trading-name") map {
      _.as[Option[String]](TradingDetails.tradingNameApiPrePopReads)
    } recover {
      case e =>
        Logger.warn(s"[BusinessRegistration][retrieveTradingName] an error occurred when retrieving trading name from business registration with message ${e.getMessage} for regID: $regId")
        None
    }

  def upsertTradingName(regId: String, tradingName: String)(implicit hc: HeaderCarrier): Future[String] = {
    implicit val prePopWrites = TradingDetails.tradingNameApiPrePopWrites
    http.POST[String,HttpResponse](s"$businessRegistrationUrl$businessRegistrationUri/$regId/trading-name", tradingName) map {
      _ => tradingName
    } recover {
      case e =>
        Logger.warn(s"[BusinessRegistration][upsertTradingName] an error occurred when upserting trading name from business registration with message ${e.getMessage} for regID: $regId")
        tradingName
    }
  }
}