/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.RegistrationConnector
import javax.inject.Inject
import models._
import models.api.Threshold
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class VoluntaryPageViewModel(
                                   form: Option[(DateSelection.Value, Option[LocalDate])],
                                   ctActive: Option[LocalDate]
                                 )

case class MandatoryDateModel(
                               calculatedDate: LocalDate,
                               startDate: Option[LocalDate],
                               selected: Option[DateSelection.Value]
                             )

class ReturnsServiceImpl @Inject()(val vatRegConnector: RegistrationConnector,
                                   val vatService: RegistrationService,
                                   val s4lService: S4LService,
                                   val prePopService: PrePopService) extends ReturnsService

trait ReturnsService {
  val vatRegConnector: RegistrationConnector
  val vatService: RegistrationService
  val s4lService: S4LService
  val prePopService: PrePopService


  def retrieveMandatoryDates(implicit profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[MandatoryDateModel] = {
    for {
      calcDate <- retrieveCalculatedStartDate
      vatDate <- getVatStartDate
    } yield {
      vatDate.fold(MandatoryDateModel(calcDate, None, None)) { startDate =>
        MandatoryDateModel(calcDate, vatDate, Some(if (startDate == calcDate) DateSelection.calculated_date else DateSelection.specific_date))
      }
    }
  }

  def calculateMandatoryStartDate(threshold: Threshold): LocalDate = {
    def calculatedCrossedThresholdDate(thresholdDate: Option[LocalDate]) = thresholdDate.map(_.withDayOfMonth(1).plusMonths(2))

    List[Option[LocalDate]](
      calculatedCrossedThresholdDate(threshold.thresholdInTwelveMonths),
      threshold.thresholdPreviousThirtyDays
    )
      .flatten
      .sortWith((date1, date2) => date1.isBefore(date2))
      .headOption match {
      case Some(earliest) => earliest
      case _ =>
        Logger.error("[ReturnsService] [calculateMandatoryStartDate] No dates could be retrieved from eligibility threshold in a mandatory flow")
        throw new RuntimeException("[ReturnsService] [calculateMandatoryStartDate] No dates could be retrieved from eligibility threshold in a mandatory flow")

    }
  }

  def getVatStartDate(implicit profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LocalDate]] = {
    getReturns map {
      returns => returns.start flatMap (_.date)
    }
  }

  def getReturns(implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    s4lService.fetchAndGetNoAux[Returns](S4LKey.returns) flatMap {
      case Some(returns) => Future.successful(returns)
      case _ => vatRegConnector.getReturns(profile.registrationId)
    } recover {
      case e =>
        Logger.warn("[ReturnsService] [getReturnsViewModel] " +
          "Exception encountered when fetching Returns model, default model used")
        Returns.empty
    }
  }

  def retrieveCalculatedStartDate(implicit profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    vatService.getThreshold(profile.registrationId) map calculateMandatoryStartDate
  }

  def retrieveCTActiveDate(implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Option[LocalDate]] = {
    prePopService.getCTActiveDate recover {
      case e => Logger.error(s"[ReturnsService][retrieveCTActiveDate] an error occured for regId: ${profile.registrationId} with message: ${e.getMessage}")
        throw e
    }
  }

  def voluntaryStartPageViewModel()(implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[VoluntaryPageViewModel] = {
    for {
      returns <- getReturns
      ctActive <- retrieveCTActiveDate
    } yield {
      VoluntaryPageViewModel((ctActive, returns.start) match {
        case (Some(cta), Some(Start(Some(vsd)))) if cta == vsd => Some((DateSelection.business_start_date, Some(vsd)))
        case (_, Some(Start(None))) => Some((DateSelection.company_registration_date, None))
        case (_, Some(Start(Some(vsd)))) => Some((DateSelection.specific_date, Some(vsd)))
        case _ => None
      }, ctActive)
    }
  }

  def saveVoluntaryStartDate
  (dateChoice: DateSelection.Value, startDate: Option[LocalDate], ctActive: Option[LocalDate])
  (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    saveVatStartDate((dateChoice, startDate, ctActive) match {
      case (DateSelection.company_registration_date, _, _) => None
      case (DateSelection.business_start_date, _, Some(cta)) => Some(cta)
      case (DateSelection.specific_date, Some(vsd), _) => Some(vsd)
      case _ => None
    })
  }

  def handleView(returns: Returns): Completion[Returns] = returns match {
    case Returns(Some(false), _, Some(_), Some(_)) =>
      Complete(returns.copy(frequency = Some(Frequency.quarterly)))

    case Returns(Some(true), Some(Frequency.quarterly), Some(_), Some(_)) =>
      Complete(returns)

    case Returns(Some(true), Some(Frequency.monthly), _, Some(_)) =>
      Complete(returns.copy(staggerStart = None))

    case _ =>
      Incomplete(returns)
  }

  def submitReturns(returns: Returns)
                   (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    handleView(returns) fold(
      incomplete =>
        s4lService.saveNoAux(returns, S4LKey.returns),

      complete =>
        vatRegConnector.patchReturns(profile.registrationId, returns) map { _ =>
          s4lService.clear
        }
    ) map { _ => returns }
  }

  def saveReclaimVATOnMostReturns(reclaimView: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    getReturns flatMap { storedData =>
      for {
        returns <- submitReturns(storedData.copy(
          reclaimVatOnMostReturns = Some(reclaimView),
          frequency = if (!reclaimView) Some(Frequency.quarterly) else storedData.frequency
        ))
      } yield {
        returns
      }
    }
  }

  def saveFrequency(frequencyView: Frequency.Value)
                   (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(frequency = Some(frequencyView)))
      )
  }

  def saveStaggerStart(staggerStartView: Stagger.Value)
                      (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(
        storedData.copy(staggerStart = Some(staggerStartView))
      )
      )
  }

  def saveVatStartDate(vatStartDateView: Option[LocalDate])
                      (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(start = Some(Start(vatStartDateView))))
      )
  }

  def getThreshold()
                  (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Boolean] = {
    vatService.getThreshold(profile.registrationId) map (!_.mandatoryRegistration)
  }

}