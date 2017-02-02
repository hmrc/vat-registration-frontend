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

import com.google.inject.ImplementedBy
import config.WSHttp
import enums.DownstreamOutcome
import models.api._
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationConnector])
trait RegistrationConnector {
  val vatRegUrl: String
  val http: HttpGet with HttpPost with HttpPatch
}

class VatRegistrationConnector extends RegistrationConnector with ServicesConfig {
  //$COVERAGE-OFF$
  val vatRegUrl = baseUrl("vat-registration")
  val http = WSHttp
  //$COVERAGE-ON$

  def createNewRegistration()(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[DownstreamOutcome.Value] = {
    http.POSTEmpty[HttpResponse](s"$vatRegUrl/vatreg/new") map {
      response => response.status match {
        case Status.CREATED => DownstreamOutcome.Success
      }
    } recover {
      case e: Exception => logResponse(e, "createNewRegistration", "creating new registration")
        DownstreamOutcome.Failure
    }
  }

  def getRegistration(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.GET[VatScheme](s"$vatRegUrl/vatreg/$regId") recover {
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

