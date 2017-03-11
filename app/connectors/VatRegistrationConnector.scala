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
import models.api._
import play.api.Logger
import play.api.http.Status
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
  val vatRegUrl: String
  val http: WSHttp

  def createNewRegistration()(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.POSTEmpty[VatScheme](s"$vatRegUrl/vatreg/new") recover {
      case e: Exception => throw logResponse(e, "createNewRegistration", "creating new registration")
    }
  }

  def getRegistration(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.GET[VatScheme](s"$vatRegUrl/vatreg/$regId/get-scheme") recover {
      case e: Exception => throw logResponse(e, "getRegistration", "getting registration")
    }
  }

  def upsertVatChoice(regId: String, vatChoice: VatChoice)(implicit hc: HeaderCarrier, rds: HttpReads[VatChoice]): Future[VatChoice] = {
    http.PATCH[VatChoice, VatChoice](s"$vatRegUrl/vatreg/$regId/vat-choice", vatChoice) recover {
      case e: Exception => throw logResponse(e, "upsertVatChoice", "upserting vat choice")
    }
  }

  def upsertVatTradingDetails(regId: String, vatTradingDetails: VatTradingDetails)
                             (implicit hc: HeaderCarrier, rds: HttpReads[VatTradingDetails]): Future[VatTradingDetails] = {
    http.PATCH[VatTradingDetails, VatTradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details", vatTradingDetails) recover {
      case e: Exception => throw logResponse(e, "upsertVatTradingDetails", "upserting trading details")
    }
  }

  def upsertVatFinancials(regId: String, vatFinancials: VatFinancials)
                             (implicit hc: HeaderCarrier, rds: HttpReads[VatFinancials]): Future[VatFinancials] = {
    http.PATCH[VatFinancials, VatFinancials](s"$vatRegUrl/vatreg/$regId/vat-financials", vatFinancials) recover {
      case e: Exception => throw logResponse(e, "upsertVatFinancials", "upserting financials details")
    }
  }

  def upsertSicAndCompliance(regId: String, sicAndCompliance: SicAndCompliance)
                         (implicit hc: HeaderCarrier, rds: HttpReads[SicAndCompliance]): Future[SicAndCompliance] = {
    http.PATCH[SicAndCompliance, SicAndCompliance](s"$vatRegUrl/vatreg/$regId/sic-and-compliance", sicAndCompliance) recover {
      case e: Exception => throw logResponse(e, "upsertSicAndCompliance", "upserting sicAndCompliance details")
    }
  }

  def deleteVatScheme(regId: String)
                     (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Boolean] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete-scheme") recover {
      case e: Exception => throw logResponse(e, "deleteVatScheme", "delete VatScheme details")
    }
  }

  def deleteBankAccount(regId: String)
                       (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Boolean] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete-bank-account") recover {
      case e: Exception => throw logResponse(e, "deleteBankAccount", "delete VatBankAccount details")
    }
  }

  def deleteZeroRatedTurnover(regId: String)
                             (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Boolean] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete-zero-rated_turnover") recover {
      case e: Exception => throw logResponse(e, "deleteZeroRatedTurnover", "delete ZeroRatedTurnoverEstimate details")
    }
  }

  def deleteAccountingPeriodStart(regId: String)
                             (implicit hc: HeaderCarrier, rds: HttpReads[Boolean]): Future[Boolean] = {
    http.DELETE[Boolean](s"$vatRegUrl/vatreg/$regId/delete-accounting-period") recover {
      case e: Exception => throw logResponse(e, "deleteAccountingPeriodStart", "delete AccountingPeriodStart details")
    }
  }

  private[connectors] def logResponse(e: Throwable, f: String, m: String): Throwable = {
    def log(s: String) = Logger.warn(s"[VatRegistrationConnector] [$f] received $s when $m")
    e match {
      case e: NotFoundException => log("NOT FOUND")
      case e: BadRequestException => log("BAD REQUEST")
      case e: Upstream4xxResponse => e.upstreamResponseCode match {
        case Status.FORBIDDEN => log("FORBIDDEN")
        case _ => log(s"Upstream 4xx: ${e.upstreamResponseCode} ${e.message}")
      }
      case e: Upstream5xxResponse => log(s"Upstream 5xx: ${e.upstreamResponseCode}")
      case e: Exception => log(s"ERROR: ${e.getMessage}")
    }
    e
  }
}


