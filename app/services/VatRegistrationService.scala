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
import connectors.{CompanyRegistrationConnector, OptionalResponse, VatRegistrationConnector}
import models._
import models.api._
import models.external.CoHoCompanyProfile
import models.view.vatFinancials.EstimateVatTurnover
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def createRegistrationFootprint()(implicit hc: HeaderCarrier): Future[Unit]

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme]

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String]

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials]

  def submitSicAndCompliance()(implicit hc: HeaderCarrier): Future[VatSicAndCompliance]

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails]

  def submitVatContact()(implicit hc: HeaderCarrier): Future[VatContact]

  def submitVatEligibility()(implicit hc: HeaderCarrier): Future[VatServiceEligibility]

  def submitVatLodgingOfficer()(implicit hc: HeaderCarrier): Future[VatLodgingOfficer]

  def deleteElement(elementPath: ElementPath)(implicit hc: HeaderCarrier): Future[Unit]

  def deleteElements(elementPath: List[ElementPath])(implicit hc: HeaderCarrier): Future[Unit]

}

class VatRegistrationService @Inject()(s4LService: S4LService,
                                       vatRegConnector: VatRegistrationConnector,
                                       compRegConnector: CompanyRegistrationConnector)
  extends RegistrationService with CommonService {


  import cats.syntax.all._

  def getFlatRateSchemeThreshold()(implicit hc: HeaderCarrier): Future[Long] =
    s4LService.getViewModel[EstimateVatTurnover, S4LVatFinancials]()
      .orElseF(getVatScheme() map ApiModelTransformer[EstimateVatTurnover].toViewModel)
      .map(_.vatTurnoverEstimate).fold(0L)(estimate => Math.round(estimate * 0.02))

  private def s4l[T: Format : S4LKey]()(implicit hc: HeaderCarrier) =
    s4LService.fetchAndGet[T]()

  private def update[C, G](c: Option[C])(implicit t: ViewModelTransformer[C, G]): G => G =
    g => c.map(t.toApi(_, g)).getOrElse(g)

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] =
    vatRegConnector.getAckRef(regId)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Unit] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def deleteElement(elementPath: ElementPath)(implicit hc: HeaderCarrier): Future[Unit] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteElement(elementPath))

  def deleteElements(elementPaths: List[ElementPath])(implicit hc: HeaderCarrier): Future[Unit] = {
    import cats.instances.list._
    elementPaths traverse_ deleteElement
  }

  def conditionalDeleteElement(elementPath: ElementPath, cond: Boolean)(implicit hc: HeaderCarrier): Future[Unit] = {
    if (cond) deleteElement(elementPath) else ().pure
  }

  def createRegistrationFootprint()(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
      optCompProfile <- compRegConnector.getCompanyRegistrationDetails(vatScheme.id).value
      _ <- optCompProfile.map(keystoreConnector.cache[CoHoCompanyProfile]("CompanyProfile", _)).pure
    } yield ()

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {
    def merge(fresh: Option[S4LVatFinancials], vs: VatScheme): VatFinancials =
      fresh.fold(
        vs.financials.getOrElse(throw fail("VatFinancials"))
      ) { s4l =>
        update(s4l.estimateVatTurnover)
          .andThen(update(s4l.zeroRatedTurnoverEstimate))
          .andThen(update(s4l.vatChargeExpectancy))
          .andThen(update(s4l.vatReturnFrequency))
          .andThen(update(s4l.accountingPeriod))
          .andThen(update(s4l.companyBankAccountDetails))
          .apply(vs.financials.getOrElse(VatFinancials.empty)) //TODO remove the "seeding" with empty
      }

    for {
      (vs, vf) <- (getVatScheme() |@| s4l[S4LVatFinancials]()).tupled
      response <- vatRegConnector.upsertVatFinancials(vs.id, merge(vf, vs))
    } yield response
  }

  def submitSicAndCompliance()(implicit hc: HeaderCarrier): Future[VatSicAndCompliance] = {
    def merge(fresh: Option[S4LVatSicAndCompliance], vs: VatScheme) =
      fresh.fold(
        vs.vatSicAndCompliance.getOrElse(throw fail("VatSicAndCompliance"))
      ) { s4l =>
        update(s4l.description)
          .andThen(update(s4l.notForProfit))
          .andThen(update(s4l.companyProvideWorkers))
          .andThen(update(s4l.workers))
          .andThen(update(s4l.temporaryContracts))
          .andThen(update(s4l.skilledWorkers))
          .andThen(update(s4l.adviceOrConsultancy))
          .andThen(update(s4l.actAsIntermediary))
          .andThen(update(s4l.chargeFees))
          .andThen(update(s4l.leaseVehicles))
          .andThen(update(s4l.additionalNonSecuritiesWork))
          .andThen(update(s4l.discretionaryInvestmentManagementServices))
          .andThen(update(s4l.investmentFundManagement))
          .andThen(update(s4l.manageAdditionalFunds))
          .andThen(update(s4l.mainBusinessActivity))
          .apply(vs.vatSicAndCompliance.getOrElse(VatSicAndCompliance.empty))
      }

    for {
      (vs, vsc) <- (getVatScheme() |@| s4l[S4LVatSicAndCompliance]()).tupled
      response <- vatRegConnector.upsertSicAndCompliance(vs.id, merge(vsc, vs))
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    def merge(fresh: Option[S4LTradingDetails], vs: VatScheme): VatTradingDetails =
      fresh.fold(
        vs.tradingDetails.getOrElse(throw fail("VatTradingDetails"))
      ) { s4l =>
        update(s4l.voluntaryRegistration)
          .andThen(update(s4l.tradingName))
          .andThen(update(s4l.startDate))
          .andThen(update(s4l.voluntaryRegistrationReason))
          .andThen(update(s4l.euGoods))
          .andThen(update(s4l.applyEori))
          .apply(vs.tradingDetails.getOrElse(VatTradingDetails.empty)) //TODO remove the "seeding" with empty
      }

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LTradingDetails]()).tupled
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, merge(vlo, vs))
    } yield response
  }

  def submitVatContact()(implicit hc: HeaderCarrier): Future[VatContact] = {
    def merge(fresh: Option[S4LVatContact], vs: VatScheme): VatContact =
      fresh.fold(
        vs.vatContact.getOrElse(throw fail("VatContact"))
      ) { s4l =>
        update(s4l.businessContactDetails)
          .apply(vs.vatContact.getOrElse(VatContact.empty)) //TODO remove the "seeding" with empty
      }

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LVatContact]()).tupled
      response <- vatRegConnector.upsertVatContact(vs.id, merge(vlo, vs))
    } yield response
  }

  def submitVatEligibility()(implicit hc: HeaderCarrier): Future[VatServiceEligibility] = {
    def merge(fresh: Option[S4LVatEligibility], vs: VatScheme): VatServiceEligibility =
      fresh.fold(
        vs.vatServiceEligibility.getOrElse(throw fail("VatServiceEligibility"))
      ) { s4l =>
        update(s4l.vatEligibility)
          .apply(vs.vatServiceEligibility.getOrElse(VatServiceEligibility()))
      }

    for {
      (vs, ve) <- (getVatScheme() |@| s4l[S4LVatEligibility]()).tupled
      response <- vatRegConnector.upsertVatEligibility(vs.id, merge(ve, vs))
    } yield response
  }

  def submitVatLodgingOfficer()(implicit hc: HeaderCarrier): Future[VatLodgingOfficer] = {
    def merge(fresh: Option[S4LVatLodgingOfficer], vs: VatScheme): VatLodgingOfficer =
      fresh.fold(
        vs.lodgingOfficer.getOrElse(throw fail("VatLodgingOfficer"))
      ) { s4l =>
        update(s4l.officerHomeAddress)
          .andThen(update(s4l.officerDateOfBirth))
          .andThen(update(s4l.officerNino))
          .andThen(update(s4l.completionCapacity))
          .andThen(update(s4l.officerContactDetails))
          .andThen(update(s4l.formerName))
          .andThen(update(s4l.formerNameDate))
          .andThen(update(s4l.previousAddress))
          .apply(vs.lodgingOfficer.getOrElse(VatLodgingOfficer.empty)) //TODO remove the "seeding" with empty
      }

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LVatLodgingOfficer]()).tupled
      response <- vatRegConnector.upsertVatLodgingOfficer(vs.id, merge(vlo, vs))
    } yield response
  }

  def submitVatFlatRateScheme()(implicit hc: HeaderCarrier): Future[VatFlatRateScheme] = {
    def merge(fresh: Option[S4LFlatRateScheme], vs: VatScheme): VatFlatRateScheme =
      fresh.fold(
        vs.vatFlatRateScheme.getOrElse(throw fail("VatFlatRateScheme"))
      ) { s4l =>
        update(s4l.joinFrs)
          .andThen(update(s4l.annualCostsInclusive))
          .andThen(update(s4l.annualCostsLimited))
          .andThen(update(s4l.categoryOfBusiness))
          .andThen(update(s4l.registerForFrs))
          .andThen(update(s4l.frsStartDate))
          .apply(vs.vatFlatRateScheme.getOrElse(VatFlatRateScheme()))
      }

    for {
      (vs, frsa) <- (getVatScheme() |@| s4l[S4LFlatRateScheme]()).tupled
      response <- vatRegConnector.upsertVatFlatRateScheme(vs.id, merge(frsa, vs))
    } yield response
  }

  def submitPpob()(implicit hc: HeaderCarrier): Future[ScrsAddress] = {

    def merge(fresh: Option[S4LPpob], vs: VatScheme): VatScheme =
      fresh.fold(
        vs
      ) { s4l =>
        s4l.address.fold(vs)(ppobview => vs.copy(ppob = ppobview.address))
      }

    for {
      (vs, vlo) <- (getVatScheme() |@| s4l[S4LPpob]()).tupled
      response <- vatRegConnector.upsertPpob(vs.id, merge(vlo, vs).ppob.getOrElse(throw fail("PPOB is null")))
    } yield response
  }

  private def fail(logicalGroup: String): Exception =
    new IllegalStateException(s"$logicalGroup data expected to be found in either backend or save for later")
}
