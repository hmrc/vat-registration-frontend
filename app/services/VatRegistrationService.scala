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
import connectors.{CompanyRegistrationConnector, OptionalResponse, VatRegistrationConnector}
import models.ModelKeys._
import models._
import models.api._
import models.external.{CoHoCompanyProfile, IncorporationInfo}
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
trait RegistrationService extends FlatRateService with LegacyServiceToBeRefactored{

  val s4LService: S4LService
  val vatRegConnector: VatRegistrationConnector
  val compRegConnector: CompanyRegistrationConnector
  val incorporationService: IncorpInfoService
}

// TODO refactor in a similar way to FRS
trait LegacyServiceToBeRefactored {

  self : RegistrationService =>

  import cats.syntax.all._

  private[services] def s4l[T: Format : S4LKey]()(implicit hc: HeaderCarrier) =
    s4LService.fetchAndGet[T]()

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] =
    vatRegConnector.getAckRef(regId)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Unit] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def createRegistrationFootprint()(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      optCompProfile <- compRegConnector.getCompanyRegistrationDetails(vatScheme.id).value
      _ <- optCompProfile.map(keystoreConnector.cache[CoHoCompanyProfile]("CompanyProfile", _)).pure
      _ <- keystoreConnector.cache[String](REGISTRATION_ID, vatScheme.id)
      _ <- incorporationService.getIncorporationInfo().map(status => keystoreConnector.cache[IncorporationInfo](INCORPORATION_STATUS, status)).value
    } yield ()

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {
    def merge(fresh: Option[S4LVatFinancials], vs: VatScheme): VatFinancials =
      fresh.fold(
        vs.financials.getOrElse(throw fail("VatFinancials"))
      ) (s4l => S4LVatFinancials.apiT.toApi(s4l))

    for {
      (vs, vf) <- (getVatScheme() |@| s4l[S4LVatFinancials]()).tupled
      response <- vatRegConnector.upsertVatFinancials(vs.id, merge(vf, vs))
    } yield response
  }

  def submitSicAndCompliance()(implicit hc: HeaderCarrier): Future[VatSicAndCompliance] = {
    def merge(fresh: Option[S4LVatSicAndCompliance], vs: VatScheme) =
      fresh.fold(
        vs.vatSicAndCompliance.getOrElse(throw fail("VatSicAndCompliance"))
      )(s4l => S4LVatSicAndCompliance.apiT.toApi(s4l))

    for {
      (vs, vsc) <- (getVatScheme() |@| s4l[S4LVatSicAndCompliance]()).tupled
      response <- vatRegConnector.upsertSicAndCompliance(vs.id, merge(vsc, vs))
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    def merge(fresh: Option[S4LTradingDetails], vs: VatScheme): VatTradingDetails =
      fresh.fold(
        vs.tradingDetails.getOrElse(throw fail("VatTradingDetails"))
      )(s4l => S4LTradingDetails.apiT.toApi(s4l))

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LTradingDetails]()).tupled
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, merge(vlo, vs))
    } yield response
  }

  def submitVatContact()(implicit hc: HeaderCarrier): Future[VatContact] = {
    def merge(fresh: Option[S4LVatContact], vs: VatScheme): VatContact =
      fresh.fold(
        vs.vatContact.getOrElse(throw fail("VatContact"))
      )(s4l => S4LVatContact.apiT.toApi(s4l))

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LVatContact]()).tupled
      response <- vatRegConnector.upsertVatContact(vs.id, merge(vlo, vs))
    } yield response
  }

  def submitVatEligibility()(implicit hc: HeaderCarrier): Future[VatServiceEligibility] = {
    def merge(fresh: Option[S4LVatEligibility], vs: VatScheme): VatServiceEligibility =
      fresh.fold(
        vs.vatServiceEligibility.getOrElse(throw fail("VatServiceEligibility"))
      )(s4l => S4LVatEligibility.apiT.toApi(s4l))

    for {
      (vs, ve) <- (getVatScheme() |@| s4l[S4LVatEligibility]()).tupled
      response <- vatRegConnector.upsertVatEligibility(vs.id, merge(ve, vs))
    } yield response
  }

  def submitVatLodgingOfficer()(implicit hc: HeaderCarrier): Future[VatLodgingOfficer] = {
    def merge(fresh: Option[S4LVatLodgingOfficer], vs: VatScheme): VatLodgingOfficer =
      fresh.fold(
        vs.lodgingOfficer.getOrElse(throw fail("VatLodgingOfficer"))
      )(s4l => S4LVatLodgingOfficer.apiT.toApi(s4l))

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LVatLodgingOfficer]()).tupled
      response <- vatRegConnector.upsertVatLodgingOfficer(vs.id, merge(vlo, vs))
    } yield response
  }

}
