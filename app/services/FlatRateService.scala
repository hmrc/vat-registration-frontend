/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import connectors.{ConfigConnector, VatRegistrationConnector}
import models.api.SicCode
import models.{FRSDateChoice, Start, _}
import play.api.Logger
import services.FlatRateService._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson

import java.time.LocalDate
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FlatRateService @Inject()(val s4LService: S4LService,
                                val sicAndComplianceService: SicAndComplianceService,
                                val configConnector: ConfigConnector,
                                val vatRegConnector: VatRegistrationConnector)(implicit ec: ExecutionContext) {

  def applyPercentRoundUp(l: Long): Long = Math.ceil(l * relevantGoodsPercent).toLong

  def getFlatRate(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    s4LService.fetchAndGet[FlatRateScheme] flatMap {
      case None | Some(FlatRateScheme(None, None, None, None, None, None, None, None, None)) =>
        vatRegConnector.getFlatRate(profile.registrationId) map {
          case Some(flatRateScheme) => flatRateScheme
          case None => FlatRateScheme()
        }
      case Some(flatRateScheme) => Future.successful(flatRateScheme)
    }

  def handleView(flatRate: FlatRateScheme): Completion[FlatRateScheme] = flatRate match {
    case FlatRateScheme(Some(true), Some(true), Some(_), Some(isOverBusinessGoodsPercent), Some(true), Some(Start(_)), _, Some(_), Some(_))
    => Complete(flatRate.copy(limitedCostTrader = Some(!isOverBusinessGoodsPercent)))
    case FlatRateScheme(Some(true), Some(false), _, _, Some(true), Some(Start(_)), _, Some(_), Some(_))
    => Complete(flatRate.copy(estimateTotalSales = None, overBusinessGoodsPercent = None, limitedCostTrader = Some(true)))
    case FlatRateScheme(Some(false), _, _, _, _, _, _, _, _)
    => Complete(flatRate)
    case _
    => Incomplete(flatRate)
  }

  def submitFlatRate(data: FlatRateScheme)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] = {
    handleView(data).fold(
      incomplete => s4LService.save[FlatRateScheme](incomplete).map(_ => incomplete),
      complete => for {
        _ <- vatRegConnector.upsertFlatRate(profile.registrationId, complete)
        _ <- s4LService.clearKey[FlatRateScheme]
      } yield complete
    )
  }

  def updateFlatRate(newS4L: FlatRateScheme => FlatRateScheme)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    getFlatRate flatMap { storedData => submitFlatRate(newS4L(storedData)) }

  def saveJoiningFRS(answer: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate(frs => if (answer) frs.copy(joinFrs = Some(answer)) else FlatRateScheme(Some(false)))

  def saveOverBusinessGoods(newValue: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate { storedData =>
      storedData.copy(
        overBusinessGoods = Some(newValue),
        useThisRate = if (storedData.overBusinessGoods.contains(newValue)) storedData.useThisRate else None,
        categoryOfBusiness = if (newValue) storedData.categoryOfBusiness else None,
        limitedCostTrader = Some(!newValue)
      )
    }

  def saveOverBusinessGoodsPercent(newValue: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate { storedData =>
      storedData.copy(
        overBusinessGoodsPercent = Some(newValue),
        useThisRate = if (storedData.overBusinessGoodsPercent.contains(newValue)) storedData.useThisRate else None,
        categoryOfBusiness = if (newValue) storedData.categoryOfBusiness else None,
        limitedCostTrader = Some((storedData.overBusinessGoods contains true) && !newValue)
      )
    }

  def saveRegister(answer: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    retrieveSectorPercent.flatMap { case (category, _, percent) =>
      updateFlatRate { storedData =>
        storedData.copy(
          joinFrs = Some(answer),
          useThisRate = Some(answer),
          categoryOfBusiness = Some(category),
          percent = Some(percent),
          frsStart = if (answer) storedData.frsStart else None)
      }
    }

  def retrieveSectorPercent(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[(String, String, BigDecimal)] = {
    getFlatRate flatMap {
      case FlatRateScheme(_, _, _, _, _, _, Some(sector), _, _) if sector.nonEmpty =>
        val (label, pct) = configConnector.getBusinessTypeDetails(sector)
        Future.successful((sector, label, pct))
      case _ =>
        sicAndComplianceService.getSicAndCompliance map { sicAndCompliance =>
          sicAndCompliance.mainBusinessActivity match {
            case Some(mainBusinessActivity) =>
              val frsId = configConnector.getSicCodeFRSCategory(mainBusinessActivity.id)
              val (label, percent) = configConnector.getBusinessTypeDetails(frsId)
              (frsId, label, percent)
            case None => throw new IllegalStateException("[FlatRateService] [retrieveSectorPercent] Can't determine main business activity")
          }
        }
    }
  }

  def saveUseFlatRate(answer: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    updateFlatRate { storedData =>
      val (_, percent) = configConnector.getBusinessTypeDetails(storedData.categoryOfBusiness.get)
      storedData.copy(
        joinFrs = Some(answer),
        useThisRate = Some(answer),
        percent = Some(percent),
        frsStart = if (answer) storedData.frsStart else None,
        limitedCostTrader = if (answer) storedData.limitedCostTrader else Some(false)
      )
    }

  def saveConfirmSector(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    retrieveSectorPercent flatMap { sectorPct =>
      val (sector, _, _) = sectorPct
      updateFlatRate(storedData => storedData.copy(
        categoryOfBusiness = Some(sector),
        percent = if (storedData.categoryOfBusiness.contains(sector)) storedData.percent else None
      ))
    }

  def clearFrs(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Boolean] = {
    s4LService.save(FlatRateScheme()) flatMap (_ =>
      vatRegConnector.clearFlatRate(cp.registrationId).map(_ => true))
  }

  def resetFRSForSAC(sicCode: SicCode)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[SicCode] = {
    for {
      mainSic <- sicAndComplianceService.getSicAndCompliance.map(_.mainBusinessActivity)
      selectionChanged = mainSic.exists(_.id != sicCode.code)
      _ <- if (selectionChanged) clearFrs else Future.successful(true)
    } yield sicCode
  }

  def saveStartDate(dateChoice: FRSDateChoice.Value, date: Option[LocalDate])(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[FlatRateScheme] =
    if (dateChoice == FRSDateChoice.VATDate) {
      fetchVatStartDate flatMap { vatStartDate =>
        updateFlatRate(_.copy(frsStart = Some(Start(vatStartDate))))
      }
    } else {
      updateFlatRate(_.copy(frsStart = Some(Start(date))))
    }

  def getPrepopulatedStartDate(vatStartDate: Option[LocalDate])(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[(Option[FRSDateChoice.Value], Option[LocalDate])] =
    getFlatRate map {
      case FlatRateScheme(_, _, _, _, _, Some(Start(frd)), _, _, _) if vatStartDate == frd => (Some(FRSDateChoice.VATDate), None)
      case FlatRateScheme(_, _, _, _, _, Some(Start(None)), _, _, _) => (Some(FRSDateChoice.VATDate), None)
      case FlatRateScheme(_, _, _, _, _, Some(Start(Some(frd))), _, _, _) => (Some(FRSDateChoice.DifferentDate), Some(frd))
      case _ => (None, None)

    }

  def fetchVatStartDate(implicit headerCarrier: HeaderCarrier, currentProfile: CurrentProfile): Future[Option[LocalDate]] = {
    vatRegConnector.getReturns(currentProfile.registrationId) map { returns =>
      returns.startDate
    } recover {
      case e =>
        Logger.warn(s"[FlatRateService] - [fetchVatStartDate] - encountered an error when retrieving the Returns block with exception: ${e.getMessage}")
        throw e
    }
  }

  def saveEstimateTotalSales(estimate: Long)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[FlatRateScheme] =
    updateFlatRate(_.copy(estimateTotalSales = Some(estimate)))

  def saveBusinessType(businessType: String)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[FlatRateScheme] =
    updateFlatRate { storedData =>
      val percent: BigDecimal = configConnector.getBusinessTypeDetails(businessType)._2
      if (storedData.categoryOfBusiness.contains(businessType) && storedData.percent.contains(percent)) {
        storedData
      } else {
        storedData.copy(useThisRate = None, categoryOfBusiness = Some(businessType), percent = None)
      }
    }
}

object FlatRateService {
  val defaultFlatRate: BigDecimal = 16.5
  val relevantGoodsPercent: Double = 0.02
}