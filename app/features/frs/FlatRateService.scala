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
import connectors.{ConfigConnector, RegistrationConnector}
import features.returns.Start
import features.sicAndCompliance.services.SicAndComplianceService
import features.turnoverEstimates.TurnoverEstimatesService
import frs.{AnnualCosts, FRSDateChoice, FlatRateScheme}
import models._
import models.api.{SicCode, VatScheme}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FlatRateServiceImpl @Inject()(val turnoverEstimateService: TurnoverEstimatesService,
                                    val s4LService: S4LService,
                                    val sicAndComplianceService: SicAndComplianceService,
                                    val configConnector : ConfigConnector,
                                    val vatRegConnector: RegistrationConnector) extends FlatRateService

trait FlatRateService  {
  protected val turnoverEstimateService : TurnoverEstimatesService
  val s4LService: S4LService
  val sicAndComplianceService: SicAndComplianceService
  val configConnector: ConfigConnector
  val vatRegConnector: RegistrationConnector

  private val LIMITED_COST_TRADER_THRESHOLD                   = 1000L
  private val defaultFlatRate: BigDecimal                     = 16.5

  def getFlatRateSchemeThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] = {
    turnoverEstimateService.fetchTurnoverEstimates map {
      case Some(estimate) => Math.round(estimate.vatTaxable * 0.02)
      case None           => 0L
    }
  }

  def getFlatRate(implicit hc: HeaderCarrier, profile: CurrentProfile, ec : ExecutionContext): Future[FlatRateScheme] =
    s4LService.fetchAndGetNoAux(FlatRateScheme.s4lkey) flatMap {
      case Some(s4l)  => Future.successful(s4l)
      case None       => vatRegConnector.getFlatRate(profile.registrationId) map {
        case Some(frs)    => frs
        case None         => FlatRateScheme()
      }
    }

  def handleView(flatRate: FlatRateScheme): Completion[FlatRateScheme] = flatRate match {
    case FlatRateScheme(Some(false), _, _, _, _, _, _, _)
      => Complete(FlatRateScheme(Some(false)))
    case FlatRateScheme(Some(true), Some(AnnualCosts.DoesNotSpend), Some(_), _, Some(false), _, Some(_), Some(_))
      => Complete(flatRate.copy(frsStart = None))
    case FlatRateScheme(Some(true), Some(_), _, _, Some(false), _, Some(_), Some(_))
      => Complete(flatRate.copy(frsStart = None))
    case FlatRateScheme(Some(true), Some(AnnualCosts.DoesNotSpend), Some(_), _, Some(true), Some(Start(_)), Some(_), Some(_))
      => Complete(flatRate)
    case FlatRateScheme(Some(true), Some(_), _, _, Some(true), Some(Start(_)), Some(_), Some(_))
      => Complete(flatRate)
    case _
      => Incomplete(flatRate)
  }

  def submitFlatRate(data : FlatRateScheme)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] = {
    handleView(data).fold(
      incomplete  => {
        s4LService.saveNoAux(incomplete, FlatRateScheme.s4lkey) map { _ => incomplete}
      },
      complete    => {
        for {
          _ <- vatRegConnector.upsertFlatRate(profile.registrationId, complete)
          _ <- s4LService.clear
        } yield {
          complete
        }
      }
    )
  }

  def updateFlatRate(newS4L : (FlatRateScheme => FlatRateScheme))(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    getFlatRate flatMap { storedData => submitFlatRate(newS4L(storedData))}

  def saveJoiningFRS(answer : Boolean)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate (storedData => if (answer) storedData.copy(Some(true)) else FlatRateScheme(Some(false)))

  def saveOverAnnualCosts(annualCosts : AnnualCosts.Value)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    getFlatRateSchemeThreshold flatMap {
      taxable => updateFlatRate { storedData =>
        storedData.copy(
          overBusinessGoods = Some(annualCosts),
          overBusinessGoodsPercent = annualCosts match {
            case AnnualCosts.DoesNotSpend => storedData.overBusinessGoodsPercent
            case _ => None
          },
          useThisRate = (annualCosts, storedData.overBusinessGoods) match {
            case (AnnualCosts.DoesNotSpend, Some(obg)) if annualCosts != obg => None
            case _ => storedData.useThisRate
          },
          frsStart = (annualCosts, storedData.overBusinessGoods) match {
            case (AnnualCosts.DoesNotSpend, Some(obg)) if annualCosts != obg => None
            case _ => storedData.frsStart
          },
          estimateTotalSales = annualCosts match {
            case AnnualCosts.DoesNotSpend => Some(taxable)
            case _ => None
          }
        )
      }
    }

  def saveOverAnnualCostsPercent(annualCosts : AnnualCosts.Value)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate (storedData => storedData.copy(overBusinessGoodsPercent = Some(annualCosts)))

  def saveRegister(answer : Boolean)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate (storedData =>
      (if (answer) storedData else storedData.copy(frsStart = None)).copy(
        useThisRate = Some(answer),
        categoryOfBusiness = Some(""),
        percent = Some(defaultFlatRate)
      )
    )

  def retrieveSectorPercent(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[(String, BigDecimal)] = {
    getFlatRate flatMap {
      case FlatRateScheme(_, _, _, _, _, _, Some(sector), Some(pct)) if sector.nonEmpty => Future.successful((sector, pct))
      case _ => sicAndComplianceService.getSicAndCompliance map { sicAndCompliance =>
        sicAndCompliance.mainBusinessActivity match {
          case Some(mainBusinessActivity) => configConnector.getBusinessTypeDetails(configConnector.getSicCodeFRSCategory(mainBusinessActivity.id))
          case None => throw new IllegalStateException("[FlatRateService] [retrieveSectorPercent] Can't determine main business activity")
        }
      }
    }
  }

  def saveUseFlatRate(answer : Boolean)(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    retrieveSectorPercent flatMap {sectorPct =>
      val (sector, pct) = sectorPct
      updateFlatRate(storedData => storedData.copy (
        useThisRate = Some(answer),
        categoryOfBusiness = Some(sector),
        percent = Some(pct),
        frsStart = if (answer) storedData.frsStart else None
      ))
    }

  def saveConfirmSector(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    retrieveSectorPercent flatMap {sectorPct =>
      val (sector, pct) = sectorPct
      updateFlatRate(storedData => storedData copy (
        categoryOfBusiness = Some(sector),
        percent = Some(pct)
      ))
    }

  def resetFRS(sicCode: SicCode)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[SicCode] = {
    for {
      mainSic          <- sicAndComplianceService.getSicAndCompliance.map(_.mainBusinessActivity)
      selectionChanged = mainSic.exists(_.id != sicCode.id)
    } yield {
      if (selectionChanged) s4LService.saveNoAux(FlatRateScheme(), FlatRateScheme.s4lkey) flatMap {
        _ => vatRegConnector.clearFlatRate(cp.registrationId)
      }

      sicCode
    }
  }

  def saveStartDate(dateChoice : FRSDateChoice.Value, date : Option[LocalDate])(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    if(dateChoice == FRSDateChoice.VATDate) {
      fetchVatStartDate flatMap {vatStartDate =>
        updateFlatRate (storedData => storedData.copy(frsStart = Some(Start(vatStartDate))))
      }
    } else {
      updateFlatRate (storedData => storedData.copy(frsStart = Some(Start(date))))
    }

  def getPrepopulatedStartDate(implicit hc : HeaderCarrier, profile: CurrentProfile): Future[(Option[FRSDateChoice.Value], Option[LocalDate])] =
    fetchVatStartDate flatMap {vatStartDate =>
      getFlatRate map {
        case FlatRateScheme(_, _, _, _, _, Some(Start(frd)), _, _) if vatStartDate == frd => (Some(FRSDateChoice.VATDate), None)
        case FlatRateScheme(_, _, _, _, _, Some(Start(None)), _, _)                       => (Some(FRSDateChoice.VATDate), None)
        case FlatRateScheme(_, _, _, _, _, Some(Start(Some(frd))), _, _)                  => (Some(FRSDateChoice.DifferentDate), Some(frd))
        case _                                                                            => (None, None)
      }
    }

  private[services] def fetchVatStartDate(implicit headerCarrier: HeaderCarrier, currentProfile: CurrentProfile) : Future[Option[LocalDate]] = {
    vatRegConnector.getReturns(currentProfile.registrationId) map {returns =>
      returns.start.flatMap(_.date)
    } recover {
      case e => None
    }
  }

  def isOverLimitedCostTraderThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] = {
    getFlatRateSchemeThreshold map (_ > LIMITED_COST_TRADER_THRESHOLD)
  }

  def saveEstimateTotalSales(estimate: Long)(implicit profile: CurrentProfile,  hc: HeaderCarrier): Future[FlatRateScheme] =
    updateFlatRate (_.copy(estimateTotalSales = Some(estimate)))

  def saveBusinessType(businessType: String)(implicit profile: CurrentProfile,  hc: HeaderCarrier): Future[FlatRateScheme] =
    updateFlatRate { storedData =>
      if (storedData.categoryOfBusiness.contains(businessType)) storedData else storedData.copy(categoryOfBusiness = Some(businessType), percent = None)
    }
}
