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

package services

import javax.inject.Inject

import common.ErrorUtil.fail
import common.enums.VatRegStatus
import connectors._
import features.turnoverEstimates.TurnoverEstimatesService
import models.ModelKeys._
import models._
import models.api._
import models.external.IncorporationInfo
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future


class VatRegistrationService @Inject()(val s4LService: S4LService,
                                       val vatRegConnector: RegistrationConnector,
                                       val compRegConnector: CompanyRegistrationConnect,
                                       val incorporationService: IncorporationInfoSrv,
                                       val keystoreConnector: KeystoreConnect,
                                       val turnoverEstimatesService: TurnoverEstimatesService) extends RegistrationService

trait RegistrationService extends FinancialsService with LegacyServiceToBeRefactored with SicAndComplianceService {
  val s4LService: S4LService
  val vatRegConnector: RegistrationConnector
  val compRegConnector: CompanyRegistrationConnect
  val incorporationService: IncorporationInfoSrv
  val turnoverEstimatesService : TurnoverEstimatesService
}

// TODO refactor in a similar way to FRS
trait LegacyServiceToBeRefactored {
  self : RegistrationService =>

  val keystoreConnector: KeystoreConnect

  private[services] def s4l[T: Format : S4LKey](implicit hc: HeaderCarrier, profile: CurrentProfile) =
    s4LService.fetchAndGet[T]

  def getVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.getRegistration(profile.registrationId)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] =
    vatRegConnector.getAckRef(regId)

  def deleteVatScheme(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Boolean] =
    vatRegConnector.deleteVatScheme(profile.registrationId)

  def createRegistrationFootprint(implicit hc: HeaderCarrier): Future[(String, String)] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration
      txId      <- compRegConnector.getTransactionId(vatScheme.id)
      status    <- incorporationService.getIncorporationInfo(txId)
      _         =   status map(x => keystoreConnector.cache[IncorporationInfo](INCORPORATION_STATUS, x))
    } yield (vatScheme.id, txId)

  def submitSicAndCompliance(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatSicAndCompliance] = {
    def merge(fresh: Option[S4LVatSicAndCompliance], vs: VatScheme) =
      fresh.fold(
        vs.vatSicAndCompliance.getOrElse(throw fail("VatSicAndCompliance"))
      )(s4l => S4LVatSicAndCompliance.apiT.toApi(s4l))

    for {
      vs       <- getVatScheme
      vsc      <- s4l[S4LVatSicAndCompliance]
      response <- vatRegConnector.upsertSicAndCompliance(profile.registrationId, merge(vsc, vs))
    } yield response
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = vatRegConnector.getStatus(regId)

  def submitRegistration()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[DESResponse] = {
    vatRegConnector.submitRegistration(profile.registrationId)
  }

  def getThreshold(regId: String)(implicit hc: HeaderCarrier): Future[Threshold] =
    vatRegConnector.getThreshold(regId) map (_.getOrElse(throw new IllegalStateException(s"No threshold block found in the back end for regId: $regId")))

}
