/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.VatRegistrationConnector
import featureswitch.core.config.FeatureSwitching
import models._
import models.api._
import models.api.returns._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsService @Inject()(val vatRegConnector: VatRegistrationConnector,
                               val vatService: VatRegistrationService,
                               val s4lService: S4LService,
                               val prePopService: PrePopulationService
                              )(implicit executionContext: ExecutionContext) extends FeatureSwitching {

  def retrieveMandatoryDates(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[MandatoryDateModel] = {
    for {
      calcDate <- retrieveCalculatedStartDate
      optVatDate <- getReturns.map(_.startDate)
    } yield {
      optVatDate.fold(MandatoryDateModel(calcDate, None, None)) { startDate =>
        MandatoryDateModel(calcDate, optVatDate, Some(if (startDate == calcDate) DateSelection.calculated_date else DateSelection.specific_date))
      }
    }
  }

  def getReturns(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    s4lService.fetchAndGet[Returns].flatMap {
      case None | Some(Returns(None, None, None, None, None, None, None, None)) => vatRegConnector.getReturns(profile.registrationId)
      case Some(returns) => Future.successful(returns)
    } recover {
      case e =>
        logger.warn("[ReturnsService] [getReturnsViewModel] " +
          "Exception encountered when fetching Returns model, default model used")
        Returns()
    }
  }

  def retrieveCalculatedStartDate(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[LocalDate] = {
    vatService.getEligibilitySubmissionData.map(
      _.calculatedDate.getOrElse(throw new InternalServerException("[ReturnsService] Missing calculated date"))
    )
  }

  def handleView(returns: Returns): Completion[Returns] = returns match {
    case Returns(_, _, _, _, _, _, _, Some(NIPCompliance(goodsToEU, None))) =>
      Incomplete(returns)
    case Returns(Some(zeroRated), Some(_), _, Some(stagger: QuarterlyStagger), _, _, _, _) =>
      Complete(returns.copy(returnsFrequency = Some(Quarterly), annualAccountingDetails = None))
    case Returns(Some(zeroRated), Some(true), Some(Monthly), _, _, _, _, _) =>
      Complete(returns.copy(staggerStart = Some(MonthlyStagger), annualAccountingDetails = None))
    case Returns(Some(zeroRated), Some(_), Some(Annual), Some(stagger: AnnualStagger), _, Some(AASDetails(Some(paymentMethod), Some(paymentFrequency))), _, _) =>
      Complete(returns)
    case _ =>
      Incomplete(returns)
  }

  def submitReturns(returns: Returns)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    handleView(returns).fold(
      incomplete => s4lService.save[Returns](incomplete),
      complete => vatRegConnector.patchReturns(profile.registrationId, complete).map { _ =>
        s4lService.clearKey[Returns]
      }
    ).map { _ => returns }
  }

  def saveZeroRatesSupplies(zeroRatedSupplies: BigDecimal)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      submitReturns(returns.copy(zeroRatedSupplies = Some(zeroRatedSupplies)))
    }
  }

  def saveReclaimVATOnMostReturns(reclaimView: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      submitReturns(returns.copy(
        reclaimVatOnMostReturns = Some(reclaimView)
      ))
    }
  }

  def saveFrequency(frequencyView: ReturnsFrequency)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      submitReturns(returns.copy(
        returnsFrequency = Some(frequencyView)
      ))
    }
  }

  def saveStaggerStart(staggerStartView: Stagger)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      submitReturns(returns.copy(staggerStart = Some(staggerStartView)))
    }
  }

  def saveVatStartDate(vatStartDateView: Option[LocalDate])(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      submitReturns(returns.copy(startDate = vatStartDateView))
    }
  }

  def saveVoluntaryStartDate(dateChoice: DateSelection.Value, startDate: Option[LocalDate], incorpDate: LocalDate)
                            (implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    val voluntaryDate = (dateChoice, startDate) match {
      case (DateSelection.company_registration_date, _) => Some(incorpDate)
      case (DateSelection.specific_date, Some(startDate)) => Some(startDate)
      case _ => None
    }

    saveVatStartDate(voluntaryDate)
  }

  def savePaymentFrequency(paymentFrequencyChoice: PaymentFrequency)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      val updatedAnnualAccountingDetails = returns.annualAccountingDetails.fold(
        AASDetails(paymentFrequency = Some(paymentFrequencyChoice))
      )(
        _.copy(paymentFrequency = Some(paymentFrequencyChoice))
      )
      submitReturns(returns.copy(annualAccountingDetails = Some(updatedAnnualAccountingDetails)))
    }
  }

  def savePaymentMethod(paymentMethodChoice: PaymentMethod)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns.flatMap { returns =>
      val oldAnnualAccountingDetails = returns.annualAccountingDetails.getOrElse(
        throw new InternalServerException("[ReturnsService][savePaymentMethod] Missing annual accounting details")
      )

      submitReturns(returns.copy(annualAccountingDetails = Some(oldAnnualAccountingDetails.copy(paymentMethod = Some(paymentMethodChoice)))))
    }
  }

  def isVoluntary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Boolean] = {
    vatService.getThreshold(profile.registrationId).map(!_.mandatoryRegistration)
  }

  def isEligibleForAAS(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[Boolean] = {
    for {
      turnoverEstimates <- vatService.fetchTurnoverEstimates
      isGroupRegistration <- vatService.getEligibilitySubmissionData.map(_.registrationReason.equals(GroupRegistration))
    } yield {
      turnoverEstimates.exists(_.turnoverEstimate <= 1350000)  && !isGroupRegistration
    }
  }

}

case class VoluntaryPageViewModel(form: Option[(DateSelection.Value, Option[LocalDate])],
                                  ctActive: Option[LocalDate])

case class MandatoryDateModel(calculatedDate: LocalDate,
                              startDate: Option[LocalDate],
                              selected: Option[DateSelection.Value])
