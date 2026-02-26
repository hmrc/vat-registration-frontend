/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.RegistrationApiConnector
import featuretoggle.FeatureToggleSupport
import models._
import models.api.vatapplication._
import play.api.libs.json.Json
import play.api.mvc.Request
import services.VatApplicationService._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import java.util
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatApplicationService @Inject()(registrationApiConnector: RegistrationApiConnector,
                                      val vatService: VatRegistrationService,
                                      applicantDetailsService: ApplicantDetailsService,
                                      timeService: TimeService,
                                      auditConnector: AuditConnector
                                     )(implicit executionContext: ExecutionContext) extends FeatureToggleSupport {

  def getVatApplication(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[VatApplication] = {
    registrationApiConnector.getSection[VatApplication](profile.registrationId).map {
      case Some(vatApplication) => vatApplication
      case None => VatApplication()
    }
  }

  def saveVatApplication[T](data: T)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[VatApplication] =
    for {
      vatApplication <- getVatApplication
      updatedVatApplication = updateModel(data, vatApplication)
      _ <- registrationApiConnector.replaceSection[VatApplication](profile.registrationId, updatedVatApplication)
    } yield updatedVatApplication

  //scalastyle:off
  private def updateModel[T](data: T, before: VatApplication)(implicit hc: HeaderCarrier, profile: CurrentProfile): VatApplication = {
    data match {
      case TradeVatGoodsOutsideUk(answer) =>
        before.copy(
          tradeVatGoodsOutsideUk = Some(answer),
          eoriRequested = if (answer) before.eoriRequested else None
        )
      case EoriRequested(answer) =>
        before.copy(eoriRequested = Some(answer))
      case StandardRate(answer) =>
        before.copy(standardRateSupplies = Some(answer))
      case ReducedRate(answer) =>
        before.copy(reducedRateSupplies = Some(answer))
      case ZeroRated(answer) =>
        val ineligibleForExemption = before.turnoverEstimate.exists(estimate => answer * 2 <= estimate)
        val updatedExemptionAnswer = if (ineligibleForExemption) None else before.appliedForExemption
        //If user changes zero rated to be less than or equal 50% of turnover, remove old exemption answer
        before.copy(
          zeroRatedSupplies = Some(answer),
          appliedForExemption = updatedExemptionAnswer
        )
      case Turnover(answer) =>
        val ineligibleForFrs = answer > 150000
        val ineligibleForAas = answer > 1350000
        val ineligibleForExemption = before.zeroRatedSupplies.exists(_ * 2 <= answer)
        if (ineligibleForFrs) registrationApiConnector.deleteSection[FlatRateScheme](profile.registrationId)
        before.copy(
          turnoverEstimate = Some(answer),
          annualAccountingDetails = if (ineligibleForAas) None else before.annualAccountingDetails,
          returnsFrequency = if (ineligibleForAas) None else before.returnsFrequency,
          appliedForExemption = if (ineligibleForExemption) None else before.appliedForExemption
        )
      case AcceptTurnOverEstimate(answer) => {
        before.copy(acceptTurnOverEstimate = Some(answer))
      }
      case ClaimVatRefunds(answer) =>
        val updatedExemptionAnswer = if (!answer) None else before.appliedForExemption
        //If user changes claim vat vatApplication answer to false, remove old exemption answer
        before.copy(
          claimVatRefunds = Some(answer),
          appliedForExemption = updatedExemptionAnswer
        )
      case AppliedForExemption(answer) =>
        before.copy(appliedForExemption = Some(answer))
      case answer: LocalDate =>
        before.copy(startDate = Some(answer))
      case answer: ReturnsFrequency =>
        answer match {
          case Monthly =>
            before.copy(
              returnsFrequency = Some(answer),
              staggerStart = Some(MonthlyStagger),
              annualAccountingDetails = None
            )
          case _ =>
            before.copy(
              returnsFrequency = Some(answer),
              staggerStart = None
            )
        }
      case answer: Stagger =>
        answer match {
          case quarterlyStagger: QuarterlyStagger =>
            before.copy(
              staggerStart = Some(answer),
              returnsFrequency = Some(Quarterly),
              annualAccountingDetails = None
            )
          case _ =>
            before.copy(staggerStart = Some(answer))
        }
      case HasTaxRepresentative(answer) =>
        before.copy(hasTaxRepresentative = Some(answer))
      case answer: NipAnswer =>
        val nipBefore = before.northernIrelandProtocol.getOrElse(NIPTurnover())
        before.copy(
          northernIrelandProtocol = Some(updateNipBlock(answer, nipBefore))
        )
      case answer: OverseasComplianceAnswer =>
        val overseasBefore = before.overseasCompliance.getOrElse(OverseasCompliance())
        before.copy(
          overseasCompliance = Some(updateOverseasBlock(answer, overseasBefore))
        )
      case answer: AasAnswer =>
        val aasBefore = before.annualAccountingDetails.getOrElse(AASDetails())
        before.copy(
          annualAccountingDetails = Some(updateAasBlock(answer, aasBefore))
        )
      case CurrentlyTrading(answer) =>
        before.copy(currentlyTrading = Some(answer))
    }
  }

  def raiseAuditEvent(vatApp: VatApplication)(implicit hc: HeaderCarrier, profile: CurrentProfile): Unit = {
    logger.info("Raising an explicit audit event!")

    val affinityGroup = if(profile.agentReferenceNumber.isDefined) "Agent" else "Organisation"

    val auditEventDetail = Json.obj(
          "journeyId"                 -> profile.registrationId,
          "userType"                  -> affinityGroup,
          "standardRateSupplies"      -> vatApp.standardRateSupplies,
          "reducedRateSupplies"       -> vatApp.reducedRateSupplies,
          "zeroRatedSupplies"         -> vatApp.zeroRatedSupplies,
          "turnoverNextTwelveMonths"  -> vatApp.turnoverEstimate,
          "acceptTurnOverEstimate"    -> vatApp.acceptTurnOverEstimate

        )
    auditConnector.sendExplicitAudit("TotalTaxableTurnover", auditEventDetail)
  }


  private def updateNipBlock[T <: NipAnswer](data: T, nipBefore: NIPTurnover): NIPTurnover =
    data match {
      case TurnoverToEu(answer) =>
        nipBefore.copy(goodsToEU = Some(answer))
      case TurnoverFromEu(answer) =>
        nipBefore.copy(goodsFromEU = Some(answer))
    }

  private def updateOverseasBlock[T <: OverseasComplianceAnswer](data: T, overseasBefore: OverseasCompliance): OverseasCompliance =
    data match {
      case GoodsToOverseas(answer) =>
        if (answer) overseasBefore.copy(goodsToOverseas = Some(answer))
        else overseasBefore.copy(
          goodsToOverseas = Some(answer),
          goodsToEu = None
        )
      case GoodsToEu(answer) =>
        overseasBefore.copy(goodsToEu = Some(answer))
      case answer: StoringGoodsForDispatch =>
        answer match {
          case StoringOverseas => overseasBefore.copy(
            storingGoodsForDispatch = Some(answer),
            usingWarehouse = None,
            fulfilmentWarehouseNumber = None,
            fulfilmentWarehouseName = None
          )
          case StoringWithinUk => overseasBefore.copy(storingGoodsForDispatch = Some(answer))
        }
      case UsingWarehouse(answer) =>
        if (answer) overseasBefore.copy(usingWarehouse = Some(answer))
        else overseasBefore.copy(
          usingWarehouse = Some(answer),
          fulfilmentWarehouseNumber = None,
          fulfilmentWarehouseName = None
        )
      case WarehouseNumber(answer) =>
        overseasBefore.copy(fulfilmentWarehouseNumber = Some(answer))
      case WarehouseName(answer) =>
        overseasBefore.copy(fulfilmentWarehouseName = Some(answer))
    }

  private def updateAasBlock[T <: AasAnswer](data: T, aasBefore: AASDetails): AASDetails =
    data match {
      case answer: PaymentFrequency =>
        aasBefore.copy(paymentFrequency = Some(answer))
      case answer: PaymentMethod =>
        aasBefore.copy(paymentMethod = Some(answer))
    }
  //scalastyle:on

  def retrieveMandatoryDates(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[MandatoryDateModel] = {
    for {
      calcDate <- retrieveCalculatedStartDate
      optVatDate <- getVatApplication.map(_.startDate)
    } yield {
      optVatDate.fold(MandatoryDateModel(calcDate, None, None)) { startDate =>
        MandatoryDateModel(calcDate, optVatDate, Some(if (startDate == calcDate) DateSelection.calculated_date else DateSelection.specific_date))
      }
    }
  }

  def retrieveCalculatedStartDate(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[LocalDate] = {
    vatService.getEligibilitySubmissionData.map(
      _.calculatedDate.getOrElse(throw new InternalServerException("[VatApplicationService] Missing calculated date"))
    )
  }
  def getReducedRated(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BigDecimal]] = {
    getVatApplication.map(_.reducedRateSupplies)
  }
  def getTurnover(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BigDecimal]] = {
    getVatApplication.map(_.turnoverEstimate)
  }

  def isEligibleForAAS(implicit hc: HeaderCarrier, currentProfile: CurrentProfile, request: Request[_]): Future[Boolean] = {
    for {
      turnoverEstimates <- getTurnover
      isGroupRegistration <- vatService.getEligibilitySubmissionData.map(_.registrationReason.equals(GroupRegistration))
    } yield {
      turnoverEstimates.exists(_ <= 1350000) && !isGroupRegistration
    }
  }

  def saveVoluntaryStartDate(dateChoice: DateSelection.Value, startDate: Option[LocalDate], incorpDate: LocalDate)
                            (implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[VatApplication] = {
    val voluntaryDate = (dateChoice, startDate) match {
      case (DateSelection.company_registration_date, _) => incorpDate
      case (DateSelection.specific_date, Some(startDate)) => startDate
    }

    saveVatApplication(voluntaryDate)
  }

  def calculateEarliestStartDate()(implicit hc: HeaderCarrier, currentProfile: CurrentProfile, request: Request[_]): Future[LocalDate] = for {
    isGroupRegistration <- vatService.getEligibilitySubmissionData.map(_.registrationReason.equals(GroupRegistration))
    dateOfIncorporationOption <-
      if (isGroupRegistration) {
        Future.successful(None)
      } else {
        applicantDetailsService.getDateOfIncorporation
      }
  } yield {
    val fourYearsAgo = timeService.minusYears(4)
    val dateOfIncorporation = dateOfIncorporationOption.getOrElse(fourYearsAgo)
    util.Collections.max(util.Arrays.asList(fourYearsAgo, dateOfIncorporation))
  }
}

case class VoluntaryPageViewModel(form: Option[(DateSelection.Value, Option[LocalDate])],
                                  ctActive: Option[LocalDate])

case class MandatoryDateModel(calculatedDate: LocalDate,
                              startDate: Option[LocalDate],
                              selected: Option[DateSelection.Value])

object VatApplicationService {
  case class TradeVatGoodsOutsideUk(answer: Boolean)

  case class EoriRequested(answer: Boolean)

  case class StandardRate(answer: BigDecimal)

  case class ReducedRate(answer: BigDecimal)

  case class ZeroRated(answer: BigDecimal)

  case class AcceptTurnOverEstimate(answer: Boolean)

  case class Turnover(answer: BigDecimal)

  case class TurnoverToEu(answer: ConditionalValue) extends NipAnswer

  case class TurnoverFromEu(answer: ConditionalValue) extends NipAnswer

  case class ClaimVatRefunds(answer: Boolean)

  case class AppliedForExemption(answer: Boolean)

  case class GoodsToOverseas(answer: Boolean) extends OverseasComplianceAnswer

  case class GoodsToEu(answer: Boolean) extends OverseasComplianceAnswer

  case class UsingWarehouse(answer: Boolean) extends OverseasComplianceAnswer

  case class WarehouseNumber(answer: String) extends OverseasComplianceAnswer

  case class WarehouseName(answer: String) extends OverseasComplianceAnswer

  case class HasTaxRepresentative(answer: Boolean)

  case class CurrentlyTrading(answer: Boolean)

  trait AasAnswer

  trait OverseasComplianceAnswer

  trait NipAnswer
}