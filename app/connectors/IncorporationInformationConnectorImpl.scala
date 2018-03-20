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
import models.CurrentProfile
import models.external.{CoHoRegisteredOfficeAddress, OfficerList}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.RegistrationWhitelist

import scala.concurrent.Future

class IncorporationInformationConnectorImpl @Inject()(val http: WSHttp, config: ServicesConfig) extends IncorporationInformationConnector {
  val incorpInfoUrl = config.baseUrl("incorporation-information")
  val incorpInfoUri = config.getConfString("incorporation-information.uri", "")
}

trait IncorporationInformationConnector extends RegistrationWhitelist {
  val incorpInfoUrl: String
  val incorpInfoUri: String
  val http: WSHttp

  def getRegisteredOfficeAddress(transactionId: String)(implicit hc: HeaderCarrier, cp:CurrentProfile): Future[Option[CoHoRegisteredOfficeAddress]] = {
    ifRegIdNotWhitelisted[Option[CoHoRegisteredOfficeAddress]](cp.registrationId) {
      http.GET[CoHoRegisteredOfficeAddress](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile").map(Some(_)).recover {
        case e: Exception =>
          logResponse(e, "getRegisteredOfficeAddress")
          None
      }
    }(returnDefaultCohoROA)
  }

  def getOfficerList(transactionId: String)(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[Option[OfficerList]] = {
    ifRegIdNotWhitelisted[Option[OfficerList]](cp.registrationId) {
      http.GET[OfficerList](s"$incorpInfoUrl$incorpInfoUri/$transactionId/officer-list").map(res =>
        Some(filterRelevantOfficers(res))
      ).recover {
        case _: NotFoundException => Some(OfficerList(items = Nil))
        case ex => throw logResponse(ex, "getOfficerList")
      }
    }(returnDefaultOfficerList)
  }

  private def filterRelevantOfficers(ol: OfficerList): OfficerList = {
    OfficerList(ol.items filter(o => (o.role.toLowerCase.equals("director") || o.role.toLowerCase.equals("secretary")) && o.resignedOn.isEmpty))
  }

  def getCompanyName(regId: String, transactionId: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    ifRegIdNotWhitelisted[JsValue](regId) {
      http.GET[JsValue](s"$incorpInfoUrl$incorpInfoUri/$transactionId/company-profile") recover {
        case notFound: NotFoundException =>
          logger.error(s"[getCompanyName] - Could not find company name for regId $regId (txId: $transactionId)")
          throw notFound
        case e =>
          logger.error(s"[getCompanyName] - There was a problem getting company for regId $regId (txId: $transactionId)", e)
          throw e
      }
    }(returnDefaultCompanyName)
  }
}
