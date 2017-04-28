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
import models.ElementPath
import models.api._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class VatRegistrationConnector extends RegistrationConnector with ServicesConfig {
  //$COVERAGE-OFF$
  val vatRegUrl = baseUrl("vat-registration")
  val http: WSHttp = WSHttp
  //$COVERAGE-ON$
}

@ImplementedBy(classOf[VatRegistrationConnector])
trait RegistrationConnector {
  self =>

  val vatRegUrl: String
  val http: WSHttp

  val className = self.getClass.getSimpleName

  def createNewRegistration()(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.POSTEmpty[VatScheme](s"$vatRegUrl/vatreg/new") recover {
      case e: Exception => throw logResponse(e, className, "createNewRegistration")
    }
  }

  def getRegistration(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.GET[VatScheme](s"$vatRegUrl/vatreg/$regId/get-scheme") recover {
      case e: Exception => throw logResponse(e, className, "getRegistration")
    }
  }

  def upsertVatChoice(regId: String, vatChoice: VatChoice)(implicit hc: HeaderCarrier, rds: HttpReads[VatChoice]): Future[VatChoice] = {
    http.PATCH[VatChoice, VatChoice](s"$vatRegUrl/vatreg/$regId/vat-choice", vatChoice) recover {
      case e: Exception => throw logResponse(e, className, "upsertVatChoice")
    }
  }

  def upsertVatTradingDetails(regId: String, vatTradingDetails: VatTradingDetails)
                             (implicit hc: HeaderCarrier, rds: HttpReads[VatTradingDetails]): Future[VatTradingDetails] = {
    http.PATCH[VatTradingDetails, VatTradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details", vatTradingDetails) recover {
      case e: Exception => throw logResponse(e, className, "upsertVatTradingDetails")
    }
  }

  def upsertVatFinancials(regId: String, vatFinancials: VatFinancials)
                         (implicit hc: HeaderCarrier, rds: HttpReads[VatFinancials]): Future[VatFinancials] = {
    http.PATCH[VatFinancials, VatFinancials](s"$vatRegUrl/vatreg/$regId/vat-financials", vatFinancials) recover {
      case e: Exception => throw logResponse(e, className, "upsertVatFinancials")
    }
  }

  def upsertSicAndCompliance(regId: String, sicAndCompliance: VatSicAndCompliance)
                            (implicit hc: HeaderCarrier, rds: HttpReads[VatSicAndCompliance]): Future[VatSicAndCompliance] = {
    http.PATCH[VatSicAndCompliance, VatSicAndCompliance](s"$vatRegUrl/vatreg/$regId/sic-and-compliance", sicAndCompliance) recover {
      case e: Exception => throw logResponse(e, className, "upsertSicAndCompliance")
    }
  }

  def upsertVatContact(regId: String, vatContact: VatContact)
                      (implicit hc: HeaderCarrier, rds: HttpReads[VatContact]): Future[VatContact] = {
    http.PATCH[VatContact, VatContact](s"$vatRegUrl/vatreg/$regId/vat-contact", vatContact) recover {
      case e: Exception => throw logResponse(e, className, "upsertVatContact")
    }
  }

  def upsertVatEligibility(regId: String, vatServiceEligibility: VatServiceEligibility)
                      (implicit hc: HeaderCarrier, rds: HttpReads[VatServiceEligibility]): Future[VatServiceEligibility] = {
    http.PATCH[VatServiceEligibility, VatServiceEligibility](s"$vatRegUrl/vatreg/$regId/service-eligibility", vatServiceEligibility) recover {
      case e: Exception => throw logResponse(e, className, "upsertVatEligibility")
    }
  }

  def deleteVatScheme(regId: String)
                     (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Unit] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete-scheme") recover {
      case e: Exception => throw logResponse(e, className, "deleteVatScheme")
    } map ( _ => ())
  }

  def deleteElement(elementPath: ElementPath)(regId: String)
                   (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Unit] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete/${elementPath.name}") recover {
      case e: Exception => throw logResponse(e, className, "deleteElement")
    } map ( _ => ())
  }

}


