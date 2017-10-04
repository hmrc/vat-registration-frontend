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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import com.google.inject.ImplementedBy
import config.WSHttp
import models.{ApiModelTransformer, CurrentProfile, S4LVatEligibilityChoice}
import models.external.{AccountingDetails, CorporationTaxRegistration}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason.INTENDS_TO_SELL
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global


@ImplementedBy(classOf[PrePopConnector])
trait PPConnector {

//  val companyRegUrl: String
  val http: WSHttp

  def getCompanyRegistrationDetails(implicit hc: HeaderCarrier,
                                    profile: CurrentProfile,
                                    rds: HttpReads[CorporationTaxRegistration]): OptionalResponse[CorporationTaxRegistration]
}


@Singleton
class PrePopConnector @Inject()(s4l: S4LService, vrs: VatRegistrationService) extends PPConnector with ServicesConfig {

  import cats.instances.future._

  //$COVERAGE-OFF$
//  val className = this.getClass.getSimpleName
//  val companyRegUrl = baseUrl("pre-pop")
  val http: WSHttp = WSHttp

  val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  override def getCompanyRegistrationDetails(implicit hc: HeaderCarrier, profile: CurrentProfile, rds: HttpReads[CorporationTaxRegistration])
  : OptionalResponse[CorporationTaxRegistration] =
    OptionT(s4l.fetchAndGet[S4LVatEligibilityChoice]()).subflatMap(_.voluntaryRegistrationReason)
      .orElseF(vrs.getVatScheme() map ApiModelTransformer[VoluntaryRegistrationReason].toViewModel).collect {
      case VoluntaryRegistrationReason(INTENDS_TO_SELL) => CorporationTaxRegistration(
        Some(AccountingDetails("", Some(LocalDate.now.plusDays(7) format expectedFormat))))
    }
  //$COVERAGE-ON$
}