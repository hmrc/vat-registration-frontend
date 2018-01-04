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
import javax.inject.Inject

import connectors.RegistrationConnector
import models.{CurrentProfile, S4LKey}
import features.financials.models._
import models.api.{VatEligibilityChoice, VatExpectedThresholdPostIncorp, VatThresholdPostIncorp}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ReturnsServiceImpl @Inject()(val vatRegConnector: RegistrationConnector,
                                   val vatService: RegistrationService,
                                   val s4lService : S4LService) extends ReturnsService

trait ReturnsService {
  val vatRegConnector: RegistrationConnector
  val vatService: RegistrationService
  val s4lService: S4LService

  //TODO: Refactor this to use the eligibility functions once this has been rebased onto it.
  def calculateMandatoryStartDate(threshold : Option[VatThresholdPostIncorp], expectedThreshold : Option[VatExpectedThresholdPostIncorp]): Option[LocalDate] = {
    def calculatedCrossedThresholdDate(thresholdDate : LocalDate) = thresholdDate.withDayOfMonth(1).plusMonths(2)

    (threshold.flatMap(_.overThresholdDate), expectedThreshold.flatMap(_.expectedOverThresholdDate)) match {
      case (Some(td), Some(ed)) =>
        val calculatedThresholdDate = calculatedCrossedThresholdDate(td)
        Some(if (calculatedThresholdDate.isBefore(ed)) calculatedThresholdDate else ed)
      case (Some(td), None) => Some(calculatedCrossedThresholdDate(td))
      case (None, Some(ed)) => Some(ed)
      case _ => None
    }
  }


  //TODO: Refactor this to use the eligibility functions once this has been rebased onto it.
  def retrieveCalculatedStartDate(implicit profile : CurrentProfile, hc : HeaderCarrier) : Future[Option[LocalDate]] = {
    vatService.getVatScheme.map(
      _.vatServiceEligibility.flatMap(
        _.vatEligibilityChoice match {
          case Some(vec) => calculateMandatoryStartDate(vec.vatThresholdPostIncorp, vec.vatExpectedThresholdPostIncorp)
          case None => None
        }
      )
    )
  }

  def getReturns(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    s4lService.fetchAndGetNoAux[Returns](S4LKey.returns) flatMap {
      case Some(returns) => Future.successful(returns)
      case _             => vatRegConnector.getReturns(profile.registrationId)
    } recover {
      case e =>
        Logger.warn("[ReturnsService] [getReturnsViewModel] " +
            "Exception encountered when fetching Returns model, default model used")
        Returns.empty
    }
  }

  private[services] def handleView(returns: Returns): Completion[Returns] = returns match {
    case Returns(Some(false), _, Some(_), Some(_)) =>
      Complete(returns.copy(frequency = Some(Frequency.quarterly)))

    case Returns(Some(true),  Some(Frequency.quarterly), Some(_), Some(_)) =>
      Complete(returns)

    case Returns(Some(true),  Some(Frequency.monthly), _, Some(_)) =>
      Complete(returns.copy(staggerStart = None))

    case _ =>
      Incomplete(returns)
  }

  def submitReturns(returns: Returns)
                   (implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    handleView(returns) fold (
      incomplete =>
        s4lService.saveNoAux(returns, S4LKey.returns),

      complete =>
        vatRegConnector.patchReturns(profile.registrationId, returns) map { _ =>
          s4lService.clear
        }
    ) map { _ => returns}
  }

  def saveReclaimVATOnMostReturns(reclaimView: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(reclaimVatOnMostReturns = Some(reclaimView))
      )
    )
  }

  def saveFrequency(frequencyView: Frequency.Value)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(frequency = Some(frequencyView)))
    )
  }

  def saveStaggerStart(staggerStartView: Stagger.Value)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(
        storedData.copy(staggerStart = Some(staggerStartView))
      )
    )
  }

  def saveVatStartDate(vatStartDateView: Option[LocalDate])(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Returns] = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(start = Some(Start(vatStartDateView))))
    )
  }

  def getEligibilityChoice()(implicit hc:HeaderCarrier, profile: CurrentProfile): Future[Boolean] = {
    vatRegConnector.getRegistration(profile.registrationId) map { vs =>
      vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice).map(_.necessity.contains(VatEligibilityChoice.NECESSITY_VOLUNTARY))
        .fold(throw new RuntimeException(""))(contains => contains)
    }
  }

}
