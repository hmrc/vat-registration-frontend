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

package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import common.ErrorUtil.fail
import common.enums.VatRegStatus
import connectors.{CompanyRegistrationConnector, OptionalResponse, VatRegistrationConnector}
import models.ModelKeys._
import models._
import models.api._
import models.external.IncorporationInfo
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class VatRegistrationService @Inject()(injS4LService: S4LService,
                                       injVatRegConnector: VatRegistrationConnector,
                                       injCompRegConnector: CompanyRegistrationConnector,
                                       injIncorporationService: IncorpInfoService)
  extends RegistrationService {

  override val s4LService = injS4LService
  override val vatRegConnector = injVatRegConnector
  override val compRegConnector = injCompRegConnector
  override val incorporationService = injIncorporationService

}

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService extends FlatRateService with TradingDetailsService with FinancialsService
  with LegacyServiceToBeRefactored with SicAndComplianceService {

  val s4LService: S4LService
  val vatRegConnector: VatRegistrationConnector
  val compRegConnector: CompanyRegistrationConnector
  val incorporationService: IncorpInfoService
}

// TODO refactor in a similar way to FRS
trait LegacyServiceToBeRefactored {

  self : RegistrationService =>


  private[services] def s4l[T: Format : S4LKey]()(implicit hc: HeaderCarrier, profile: CurrentProfile) =
    s4LService.fetchAndGet[T]()

  def getVatScheme()(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.getRegistration(profile.registrationId)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] =
    vatRegConnector.getAckRef(regId)

  def deleteVatScheme()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Unit] =
    vatRegConnector.deleteVatScheme(profile.registrationId)

  def createRegistrationFootprint()(implicit hc: HeaderCarrier): Future[(String, String)] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      txId      <- compRegConnector.getTransactionId(vatScheme.id)
      _         <- incorporationService.getIncorporationInfo(txId).map {
        status => keystoreConnector.cache[IncorporationInfo](INCORPORATION_STATUS, status)
      }.value
    } yield (vatScheme.id, txId)

  def submitSicAndCompliance()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatSicAndCompliance] = {
    def merge(fresh: Option[S4LVatSicAndCompliance], vs: VatScheme) =
      fresh.fold(
        vs.vatSicAndCompliance.getOrElse(throw fail("VatSicAndCompliance"))
      )(s4l => S4LVatSicAndCompliance.apiT.toApi(s4l))

    for {
      vs       <- getVatScheme()
      vsc      <- s4l[S4LVatSicAndCompliance]()
      response <- vatRegConnector.upsertSicAndCompliance(profile.registrationId, merge(vsc, vs))
    } yield response
  }

  def submitVatContact()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatContact] = {
    def merge(fresh: Option[S4LVatContact], vs: VatScheme): VatContact =
      fresh.fold(
        vs.vatContact.getOrElse(throw fail("VatContact"))
      )(s4l => S4LVatContact.apiT.toApi(s4l))

    for {
      vs       <- getVatScheme()
      vlo      <- s4l[S4LVatContact]()
      response <- vatRegConnector.upsertVatContact(profile.registrationId, merge(vlo, vs))
    } yield response
  }

  def submitVatEligibility()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatServiceEligibility] = {
    def merge(fresh: Option[S4LVatEligibility], vs: VatScheme): VatServiceEligibility =
      fresh.fold(
        vs.vatServiceEligibility.getOrElse(throw fail("VatServiceEligibility"))
      )(s4l => S4LVatEligibility.apiT.toApi(s4l))

    for {
      vs       <- getVatScheme()
      ve       <- s4l[S4LVatEligibility]()
      response <- vatRegConnector.upsertVatEligibility(profile.registrationId, merge(ve, vs))
    } yield response
  }

  def submitVatLodgingOfficer()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatLodgingOfficer] = {
    def merge(fresh: Option[S4LVatLodgingOfficer], vs: VatScheme): VatLodgingOfficer =
      fresh.fold(
        vs.lodgingOfficer.getOrElse(throw fail("VatLodgingOfficer"))
      )(s4l => S4LVatLodgingOfficer.apiT.toApi(s4l))

    for {
      vs       <- getVatScheme()
      vlo      <- s4l[S4LVatLodgingOfficer]()
      response <- vatRegConnector.upsertVatLodgingOfficer(profile.registrationId, merge(vlo, vs))
    } yield response
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] =
    vatRegConnector.getStatus(regId)

}
