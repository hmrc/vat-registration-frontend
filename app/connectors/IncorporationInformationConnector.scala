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

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import com.google.inject.ImplementedBy
import config.WSHttp
import models.external.{CoHoRegisteredOfficeAddress, OfficerList}
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IncorporationInformationConnector @Inject()(config: ServicesConfig) extends IncorporationInformationConnect {
  val incorpInfoUrl = config.baseUrl("incorporation-information")
  val incorpInfoUri = config.getConfString("incorporation-information.uri", "")
  val http: WSHttp = WSHttp
}

@ImplementedBy(classOf[IncorporationInformationConnector])
trait IncorporationInformationConnect { self =>

  val incorpInfoUrl: String
  val incorpInfoUri: String
  val http: WSHttp

  val className = self.getClass.getSimpleName

  def getRegisteredOfficeAddress(transactionId: String)(implicit hc: HeaderCarrier): OptionalResponse[CoHoRegisteredOfficeAddress] =
    OptionT ( http.GET[CoHoRegisteredOfficeAddress](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile").map(Some(_)).recover{
      case e: Exception => logResponse(e, className, "getRegisteredOfficeAddress")
      Option.empty[CoHoRegisteredOfficeAddress]
    })

  def getOfficerList(transactionId: String)(implicit hc: HeaderCarrier): OptionalResponse[OfficerList] =
    OptionT( http.GET[OfficerList](s"$incorpInfoUrl$incorpInfoUri/$transactionId/officer-list").map( res =>
      Some(filterRelevantOfficers(res))
    ).recover{
      case notFoundException: NotFoundException => Some(OfficerList(items = Nil))
      case ex => logResponse(ex, className, "getOfficerList")
        throw ex
    })

  private def filterRelevantOfficers(ol: OfficerList): OfficerList = {
    OfficerList(ol.items filter ( o =>
        (o.role.equals("director") || o.role.equals("secretary")) && o.resignedOn.isEmpty
      ))
  }

  def getCompanyName(redId: String, transactionId: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET[JsValue](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile") recover {
      case notFound: NotFoundException =>
        Logger.error(s"[IncorporationInformationConnector] - [getCompanyName] - Could not find company name for regId $redId (txId: $transactionId)")
        throw notFound
      case e =>
        Logger.error(s"[IncorporationInformationConnector] - [getCompanyName] - There was a problem getting company for regId $redId (txId: $transactionId)", e)
        throw e
    }
  }
}
