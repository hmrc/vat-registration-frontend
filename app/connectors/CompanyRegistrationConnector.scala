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
import models.external.CorporationTaxRegistration
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CompanyRegistrationConnector extends CTConnector with ServicesConfig {
  //$COVERAGE-OFF$
  val companyRegUrl = baseUrl("company-registration")
  val http: WSHttp = WSHttp
  //$COVERAGE-ON$
}

@ImplementedBy(classOf[CompanyRegistrationConnector])
trait CTConnector {

  val companyRegUrl: String
  val http: WSHttp

  def getCompanyRegistrationDetails
  (regId: String)
  (implicit hc: HeaderCarrier, rds: HttpReads[CorporationTaxRegistration]): OptionT[Future, CorporationTaxRegistration] =
    OptionT(
      http.GET[CorporationTaxRegistration](s"$companyRegUrl/company-registration/corporation-tax-registration/$regId/corporation-tax-registration")
        .map(Option.apply)
        .recover {
          case ex =>
            logResponse(ex, "getRegistration", "getting company registration details")
            Option.empty[CorporationTaxRegistration]
        }
    )


  private[connectors] def logResponse(e: Throwable, f: String, m: String): Unit = {
    def log(s: String) = Logger.warn(s"[CTConnector] [$f] received $s when $m")

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
  }
}


