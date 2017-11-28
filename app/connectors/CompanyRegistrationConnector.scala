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

package connectors

import javax.inject.Inject

import config.WSHttp
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class CompanyRegistrationConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends CompanyRegistrationConnect {
  val companyRegistrationUrl: String = config.baseUrl("company-registration")
  val companyRegistrationUri: String = config.getConfString("company-registration.uri", "")
}

trait CompanyRegistrationConnect {

  val companyRegistrationUrl: String
  val companyRegistrationUri: String
  val http: WSHttp

  def getTransactionId(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    http.GET[JsValue](s"$companyRegistrationUrl$companyRegistrationUri/$regId/corporation-tax-registration") map {
      _.\("confirmationReferences").\("transaction-id").as[String]
    } recover {
      case e => throw logResponse(e,"getTransactionID")
    }
  }
}



