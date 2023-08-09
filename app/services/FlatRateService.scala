/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{ConfigConnector, RegistrationApiConnector}
import models._
import models.api.SicCode
import models.error.MissingAnswerException
import services.FlatRateService._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import java.time.LocalDate
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode
import play.api.mvc.Request

@Singleton
class FlatRateService @Inject()(val businessService: BusinessService,
                                vatApplicationService: VatApplicationService,
                                val configConnector: ConfigConnector,
                                registrationApiConnector: RegistrationApiConnector)(implicit ec: ExecutionContext) {

  val missingFrsSection = "tasklist.vatRegistration.flatRateScheme"
  val missingBusinessActivitySection = "tasklist.aboutTheBusiness.businessActivities"
  val missingRegDateSection = "tasklist.vatRegistration.registrationDate"

  def applyPercentRoundUp(b: BigDecimal): BigDecimal = (b * relevantGoodsPercent).setScale(0, RoundingMode.CEILING)

  def getFlatRate(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[FlatRateScheme] = {
    registrationApiConnector.getSection[FlatRateScheme](profile.registrationId) map {
      case Some(flatRateScheme) => flatRateScheme
      case None => FlatRateScheme()
    }
  }

  def saveFlatRate[T](data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[FlatRateScheme] =
    for {
      flatRateScheme <- getFlatRate
      updatedFlatRateScheme = updateModel(data, flatRateScheme)
      result <- registrationApiConnector.replaceSection[FlatRateScheme](profile.registrationId, updatedFlatRateScheme)
    } yield result

  //scalastyle:off
  private def updateModel[T](data: T, scheme: FlatRateScheme)(implicit request: Request[_]): FlatRateScheme =
    data match {
      case JoinFrsAnswer(answer) =>
        if (answer) {
          scheme.copy(joinFrs = Some(true))
        } else {
          FlatRateScheme(joinFrs = Some(false))
        }

      case OverBusinessGoodsAnswer(answer) =>
        if (answer) {
          scheme.copy(
            overBusinessGoods = Some(true),
            useThisRate = if (scheme.overBusinessGoods.contains(answer)) scheme.useThisRate else None,
            limitedCostTrader = Some(false)
          )
        } else {
          scheme.copy(
            overBusinessGoods = Some(false),
            useThisRate = if (scheme.overBusinessGoods.contains(answer)) scheme.useThisRate else None,
            estimateTotalSales = None,
            overBusinessGoodsPercent = None,
            categoryOfBusiness = None,
            percent = None,
            limitedCostTrader = Some(true)
          )
        }

      case EstimateTotalSalesAnswer(answer) =>
        scheme.copy(
          estimateTotalSales = Some(answer)
        )

      case OverBusinessGoodsPercentAnswer(answer) =>
        if (answer) {
          scheme.copy(
            overBusinessGoodsPercent = Some(true),
            useThisRate = if (scheme.overBusinessGoodsPercent.contains(answer)) scheme.useThisRate else None,
            limitedCostTrader = Some(false)
          )
        } else {
          scheme.copy(
            overBusinessGoodsPercent = Some(false),
            useThisRate = if (scheme.overBusinessGoodsPercent.contains(answer)) scheme.useThisRate else None,
            categoryOfBusiness = None,
            percent = None,
            limitedCostTrader = Some(true)
          )
        }

      case CategoryOfBusinessAnswer(answer) =>
        val percent: BigDecimal = configConnector.getBusinessType(answer).percentage
        if (scheme.categoryOfBusiness.contains(answer) && scheme.percent.contains(percent)) {
          scheme
        } else {
          scheme.copy(categoryOfBusiness = Some(answer), useThisRate = None, percent = None)
        }

      case UseThisRateAnswer(answer) =>
        if (answer) {
          val percent = configConnector
            .getBusinessType(scheme.categoryOfBusiness.getOrElse(throw MissingAnswerException(missingFrsSection)))
            .percentage
          scheme.copy(
            useThisRate = Some(true),
            percent = Some(percent)
          )
        } else {
          FlatRateScheme(joinFrs = Some(false))
        }

      case answer: LocalDate =>
        scheme.copy(frsStart = Some(answer))

      case updateFunction: (FlatRateScheme => FlatRateScheme) =>
        updateFunction(scheme)
    }
  //scalastyle:on

  def saveRegister(answer: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[FlatRateScheme] = {
    if (answer) {
      retrieveBusinessTypeDetails.flatMap { businessType =>
        saveFlatRate { storedData: FlatRateScheme =>
          storedData.copy(
            useThisRate = Some(answer),
            categoryOfBusiness = Some(businessType.id),
            percent = Some(businessType.percentage)
          )
        }
      }
    } else {
      saveFlatRate { _: FlatRateScheme => FlatRateScheme(joinFrs = Some(false)) }
    }
  }

  def saveConfirmSector(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[FlatRateScheme] =
    retrieveBusinessTypeDetails.flatMap { businessType =>
      saveFlatRate { storedData: FlatRateScheme =>
        storedData.copy(
          categoryOfBusiness = Some(businessType.id),
          percent = None
        )
      }
    }

  def retrieveBusinessTypeDetails(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[FrsBusinessType] = {
    getFlatRate flatMap {
      case FlatRateScheme(_, _, _, _, _, _, Some(sector), _, _) if sector.nonEmpty =>
        val businessType = configConnector.getBusinessType(sector)
        Future.successful(businessType)
      case _ =>
        businessService.getBusiness map { businessDetails =>
          businessDetails.mainBusinessActivity match {
            case Some(mainBusinessActivity) =>
              val frsId = configConnector.getSicCodeFRSCategory(mainBusinessActivity.code)
              configConnector.getBusinessType(frsId)
            case None => throw MissingAnswerException(missingBusinessActivitySection)
          }
        }
    }
  }

  def resetFRSForSAC(sicCode: SicCode)(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[SicCode] = {
    for {
      mainSic <- businessService.getBusiness.map(_.mainBusinessActivity)
      selectionChanged = mainSic.exists(_.code != sicCode.code)
      _ <- if (selectionChanged) {
        registrationApiConnector.deleteSection[FlatRateScheme](cp.registrationId)
      } else {
        Future.successful(true)
      }
    } yield sicCode
  }

  def saveStartDate(dateChoice: FRSDateChoice.Value, date: Option[LocalDate])(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[FlatRateScheme] = {
    (dateChoice, date) match {
      case (FRSDateChoice.VATDate, _) =>
        fetchVatStartDate.flatMap { vatStartDate =>
          saveFlatRate(vatStartDate)
        }
      case (FRSDateChoice.DifferentDate, Some(date)) =>
        saveFlatRate(date)
      case _ =>
        throw new InternalServerException("[FlatRateService] Attempted to save invalid start date choice")
    }
  }

  def getPrepopulatedStartDate(vatStartDate: LocalDate)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[(Option[FRSDateChoice.Value], Option[LocalDate])] =
    getFlatRate map {
      case FlatRateScheme(_, _, _, _, _, Some(frd), _, _, _) if vatStartDate == frd => (Some(FRSDateChoice.VATDate), None)
      case FlatRateScheme(_, _, _, _, _, Some(frd), _, _, _) => (Some(FRSDateChoice.DifferentDate), Some(frd))
      case _ => (None, None)
    }

  def fetchVatStartDate(implicit headerCarrier: HeaderCarrier, currentProfile: CurrentProfile, request: Request[_]): Future[LocalDate] = {
    vatApplicationService.getVatApplication.map(_.startDate.getOrElse(
      throw MissingAnswerException(missingRegDateSection)
    )).recoverWith {
      case _ => vatApplicationService.retrieveCalculatedStartDate
    }
  }
}

object FlatRateService {
  val defaultFlatRate: BigDecimal = 16.5
  val relevantGoodsPercent: Double = 0.02

  case class JoinFrsAnswer(answer: Boolean)

  case class OverBusinessGoodsAnswer(answer: Boolean)

  case class EstimateTotalSalesAnswer(answer: BigDecimal)

  case class OverBusinessGoodsPercentAnswer(answer: Boolean)

  case class UseThisRateAnswer(answer: Boolean)

  case class CategoryOfBusinessAnswer(answer: String)
}