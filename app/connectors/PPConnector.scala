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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import models.CurrentProfile
import models.api.Threshold
import models.external.{AccountingDetails, CorporationTaxRegistration}
import play.api.Logger
import services.{RegistrationService, S4LService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class PrePopConnector @Inject()(val s4l: S4LService, val vrs: RegistrationService) extends PPConnector

trait PPConnector {

  val s4l: S4LService
  val vrs: RegistrationService

  val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCompanyRegistrationDetails(implicit hc: HeaderCarrier,
                                             profile: CurrentProfile,
                                             rds: HttpReads[CorporationTaxRegistration]): Future[Option[CorporationTaxRegistration]] = {

    vrs.getThreshold(profile.registrationId).map(_.voluntaryReason collect  {
      case Threshold.INTENDS_TO_SELL => CorporationTaxRegistration(
        Some(AccountingDetails("", Some(LocalDate.now.plusDays(7) format expectedFormat))))
    }) recover {
      case e => Logger.error(s"[PPConnector][getCompanyRegistrationDetails] an error occured for regId: ${profile.registrationId} with message: ${e.getMessage}")
        throw e
    }
  }
}
