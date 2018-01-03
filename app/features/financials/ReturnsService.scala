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

  def saveReclaimVATOnMostReturns(reclaimView: Boolean)(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(reclaimVatOnMostReturns = Some(reclaimView))
      )
    )
  }

  def saveFrequency(frequencyView: Frequency.Value)(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(frequency = Some(frequencyView)))
    )
  }

  def saveStaggerStart(staggerStartView: Stagger.Value)(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    getReturns flatMap (storedData =>
      submitReturns(
        storedData.copy(staggerStart = Some(staggerStartView))
      )
    )
  }

  def saveVatStartDate(vatStartDateView: LocalDate)(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    getReturns flatMap (storedData =>
      submitReturns(storedData.copy(vatStartDate = Some(vatStartDateView)))
    )
  }
}
