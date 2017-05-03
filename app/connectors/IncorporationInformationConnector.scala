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

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import config.WSHttp
import models.external.CoHoRegisteredOfficeAddress
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IncorporationInformationConnector extends IncorporationInformationConnect with ServicesConfig {
  lazy val incorpInfoUrl = baseUrl("incorporation-information")
  lazy val incorpInfoUri = getConfString("incorporation-information.uri","")
  val http : WSHttp = WSHttp
}

@ImplementedBy(classOf[IncorporationInformationConnector])
trait IncorporationInformationConnect {

  val incorpInfoUrl: String
  val incorpInfoUri: String
  val http: WSHttp

  def getRegisteredOfficeAddress(transactionId: String)(implicit hc : HeaderCarrier): Future[CoHoRegisteredOfficeAddress] = {
    http.GET[CoHoRegisteredOfficeAddress](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile") map { roAddress =>
      roAddress
    } recover {
      case badRequestErr: BadRequestException =>
        Logger.error("[CohoAPIConnector] [getRegisteredOfficeAddress] - Received a BadRequest status code when expecting a Registered office address")
        throw badRequestErr
      case ex: Exception =>
        Logger.error(s"[CohoAPIConnector] [getRegisteredOfficeAddress] - Received an error response when expecting a Registered office address - error: ${ex.getMessage}")
        throw ex
    }
  }

}
