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

import cats.data.OptionT
import com.google.inject.ImplementedBy
import config.WSHttp
import models.external.CoHoRegisteredOfficeAddress
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class IncorporationInformationConnector extends IncorporationInformationConnect with ServicesConfig {
  //$COVERAGE-OFF$
  val incorpInfoUrl = baseUrl("incorporation-information")
  val incorpInfoUri = getConfString("incorporation-information.uri", "")
  val http: WSHttp = WSHttp
  //$COVERAGE-ON$
}

@ImplementedBy(classOf[IncorporationInformationConnector])
trait IncorporationInformationConnect {
  self =>

  val incorpInfoUrl: String
  val incorpInfoUri: String
  val http: WSHttp

  val className = self.getClass.getSimpleName

  // get the registered office address from II or return None
  def getRegisteredOfficeAddress(transactionId: String)(implicit hc: HeaderCarrier): OptionalResponse[CoHoRegisteredOfficeAddress] = {
    OptionT {
      http.GET[CoHoRegisteredOfficeAddress](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile").map(Some(_)) recover {
        case e: Exception => logResponse(e, className, "getRegisteredOfficeAddress")
          Option.empty[CoHoRegisteredOfficeAddress]
      }
    }
  }
}
