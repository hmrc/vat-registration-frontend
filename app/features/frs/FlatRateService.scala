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

import java.time.LocalDate

import com.google.inject.Inject
import common.ErrorUtil.fail
import connectors.{ConfigConnector, RegistrationConnector}
import features.sicAndCompliance.services.SicAndComplianceService
import features.turnoverEstimates.TurnoverEstimatesService
import models.AnnualCostsLimitedView.{NO, YES, YES_WITHIN_12_MONTHS}
import models._
import models.api.{SicCode, VatFlatRateScheme, VatScheme}
import org.apache.commons.lang3.StringUtils
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class FlatRateServiceImpl @Inject()(val turnoverEstimateService: TurnoverEstimatesService,
                                    val s4LService: S4LService,
                                    val vatService: RegistrationService,
                                    val sicAndComplianceService: SicAndComplianceService,
                                    val configConnect : ConfigConnector,
                                    val vatRegConnector: RegistrationConnector) extends FlatRateService

trait FlatRateService  {
  protected val turnoverEstimateService : TurnoverEstimatesService
  val s4LService: S4LService
  val vatService: RegistrationService
  val sicAndComplianceService: SicAndComplianceService
  val configConnect: ConfigConnector
  val vatRegConnector: RegistrationConnector

  private val flatRateSchemeS4LKey: S4LKey[S4LFlatRateScheme] = S4LFlatRateScheme.vatFlatRateScheme
  private val LIMITED_COST_TRADER_THRESHOLD                   = 1000L
  private val defaultFlatRate: BigDecimal                     = 16.5

  type SavedFlatRateScheme = Either[S4LFlatRateScheme, VatFlatRateScheme]

  def getFlatRateSchemeThreshold()(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] = {
    turnoverEstimateService.fetchTurnoverEstimates map {
      case Some(estimate) => Math.round(estimate.vatTaxable * 0.02)
      case None           => 0L
    }
  }

  def fetchFlatRateScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LFlatRateScheme] = {
    fetchFRSFromS4L flatMap {
      case Some(frs) => Future.successful(frs)
      case None      => vatService.getVatScheme map { vatScheme =>
        vatScheme.vatFlatRateScheme match {
          case Some(_) => frsApiToView(vatScheme)
          case None    => S4LFlatRateScheme()
        }
      }
    }
  }

  def saveJoinFRS(joinFRSView: JoinFrsView)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    if (joinFRSView.selection) {
      fetchFlatRateScheme flatMap { frs =>
        saveFRS(frs.copy(joinFrs = Some(joinFRSView)))
      }
    } else {
      saveFRStoAPI(frsViewToApi(S4LFlatRateScheme(joinFrs = Some(joinFRSView)))) map Right.apply
    }
  }

  def businessSectorView()(implicit headerCarrier: HeaderCarrier, profile: CurrentProfile): Future[BusinessSectorView] = {
    fetchFlatRateScheme flatMap { flatRateScheme =>
      //TODO StringUtils.isNotBlank(???) - use ???.trim.nonEmpty ?
      flatRateScheme.categoryOfBusiness match {
        case Some(categoryOfBusiness) if StringUtils.isNotBlank(categoryOfBusiness.businessSector) => Future.successful(categoryOfBusiness)
        case _ => sicAndComplianceService.getSicAndCompliance map { sicAndCompliance =>
          sicAndCompliance.mainBusinessActivity match {
            case Some(mainBusinessActivity) => configConnect.getBusinessSectorDetails(mainBusinessActivity.id)
            case None => throw new IllegalStateException("Can't determine main business activity")
          }
        }
      }
    }
  }

  def saveAnnualCostsInclusive(annualCostsInclusive: AnnualCostsInclusiveView)
                              (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    saveFRS(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(annualCostsInclusive)))
  }

  def saveAnnualCostsLimited(annualCostsLimitedView: AnnualCostsLimitedView)
                            (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    fetchFlatRateScheme flatMap { frs =>
      annualCostsLimitedView.selection match {
        case NO                         => saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView)))
        case YES | YES_WITHIN_12_MONTHS => saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView), frsStartDate = None, categoryOfBusiness = None))
      }
    }
  }

  def saveBusinessSector(businessSectorView: BusinessSectorView)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    fetchFlatRateScheme flatMap { frs =>
      saveFRS(frs.copy(categoryOfBusiness = Some(businessSectorView)))
    }
  }

  private[services] def fetchVatStartDate(implicit headerCarrier: HeaderCarrier, currentProfile: CurrentProfile) : Future[Option[LocalDate]] = {
    vatRegConnector.getReturns(currentProfile.registrationId) map {returns =>
      returns.start.flatMap(_.date)
    } recover {
      case e => None
    }
  }

  def saveFRSStartDate(frsStartDateView: FrsStartDateView)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    fetchFlatRateScheme flatMap { frs =>
      if(frsStartDateView.dateType == FrsStartDateView.VAT_REGISTRATION_DATE) {
        fetchVatStartDate flatMap {vatStartDate =>
          saveFRS(frs.copy(frsStartDate = Some(frsStartDateView.copy(date = vatStartDate))))
        }
      } else {
        saveFRS(frs.copy(frsStartDate = Some(frsStartDateView)))
      }
    }
  }

  def saveRegisterForFRS(registerForFrs: Boolean, sector: Option[BusinessSectorView] = None)
                        (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {

    val businessSector = sector.fold(BusinessSectorView("", defaultFlatRate))(identity)

    def buildRegisterForFRS(frs: S4LFlatRateScheme) = {
      if(registerForFrs) {
        saveFRS(frs.copy(registerForFrs = Some(RegisterForFrsView(true))))
      } else {
        saveFRS(frs.copy(registerForFrs = None, frsStartDate = None))
      }
    }

    for {
      frs      <- fetchFlatRateScheme
      _        <- saveBusinessSector(businessSector)
      savedFRS <- buildRegisterForFRS(frs)
    } yield savedFRS
  }

  def isOverLimitedCostTraderThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] = {
    getFlatRateSchemeThreshold map (_ > LIMITED_COST_TRADER_THRESHOLD)
  }

  private[services] def frsApiToView(vs: VatScheme): S4LFlatRateScheme = S4LFlatRateScheme(
    joinFrs               = ApiModelTransformer[JoinFrsView].toViewModel(vs),
    annualCostsInclusive  = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
    annualCostsLimited    = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
    registerForFrs        = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
    frsStartDate          = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
    categoryOfBusiness    = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
  )

  private[services] def frsViewToApi(view: S4LFlatRateScheme): VatFlatRateScheme = VatFlatRateScheme(
    joinFrs                 = view.joinFrs.exists(_.selection),
    annualCostsInclusive    = view.annualCostsInclusive.map(_.selection),
    annualCostsLimited      = view.annualCostsLimited.map(_.selection),
    doYouWantToUseThisRate  = view.registerForFrs.map(_.selection),
    whenDoYouWantToJoinFrs  = view.frsStartDate.map(_.dateType),
    startDate               = view.frsStartDate.flatMap(_.date),
    categoryOfBusiness      = view.categoryOfBusiness.map(_.businessSector),
    percentage              = view.categoryOfBusiness.map(_.flatRatePercentage)
  )

  private[services] def fetchFRSFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LFlatRateScheme]] = {
    s4LService.fetchAndGetNoAux(flatRateSchemeS4LKey)
  }

  private[services] def fetchFRSFromAPI(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[VatFlatRateScheme]] = {
    vatService.getVatScheme map (_.vatFlatRateScheme)
  }

  private[services] def saveFRS(frs: S4LFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
    // TODO defaulting to S4L
    // TODO check needed to determine whether block is full is unreliable - awaiting API changes
    // TODO (options with no way of identifying whether they should be populated)
    // TODO on save to backend, wipe S4L for safety like PAYE?
    saveFRSToS4L(frs) map Left.apply
  }

  private[services] def saveFRSToS4L(frsView: S4LFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LFlatRateScheme] = {
    s4LService.saveNoAux[S4LFlatRateScheme](frsView, flatRateSchemeS4LKey) map (_ => frsView)
  }

  private[services] def saveFRStoAPI(frs: VatFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatFlatRateScheme] = {
    vatRegConnector.upsertVatFlatRateScheme(profile.registrationId, frs)
  }

  def resetFRS(sicCode: SicCode)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[SicCode] = {
    for {
      mainSic          <- sicAndComplianceService.getSicAndCompliance.map(_.mainBusinessActivity)
      selectionChanged = mainSic.exists(_.id != sicCode.id)
    } yield {
      if (selectionChanged) s4LService.saveNoAux(S4LFlatRateScheme(), flatRateSchemeS4LKey).flatMap(_ => submitVatFlatRateScheme())

      sicCode
    }
  }

  @deprecated
  def submitVatFlatRateScheme()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatFlatRateScheme] = {
    def merge(fresh: Option[S4LFlatRateScheme], vs: VatScheme): VatFlatRateScheme = fresh.fold(
      vs.vatFlatRateScheme.getOrElse(throw fail("VatFlatRateScheme"))
    )(s4l => S4LFlatRateScheme.apiT.toApi(s4l))

    for {
      vs <- vatService.getVatScheme
      frs <- vatService.s4l[S4LFlatRateScheme]
      response <- vatRegConnector.upsertVatFlatRateScheme(profile.registrationId, merge(frs, vs))
    } yield response
  }
}
