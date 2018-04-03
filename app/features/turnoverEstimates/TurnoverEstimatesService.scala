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

package features.turnoverEstimates

import javax.inject.Inject

import connectors.VatRegistrationConnector
import features.frs.services.FlatRateService
import frs.FlatRateScheme
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TurnoverEstimatesServiceImpl @Inject()(val vatRegConnector: VatRegistrationConnector,
                                             val frsService: FlatRateService
                                            ) extends TurnoverEstimatesService

trait TurnoverEstimatesService {

  val vatRegConnector: VatRegistrationConnector
  val frsService: FlatRateService

  def fetchTurnoverEstimates(implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[Option[TurnoverEstimates]] = {
    vatRegConnector.getTurnoverEstimates
  }

  def saveTurnoverEstimates(turnoverEstimates: TurnoverEstimates)
                           (implicit hc: HeaderCarrier, profile: CurrentProfile, ec: ExecutionContext): Future[TurnoverEstimates] = {
    val frsShouldClear = for {
      _ <- vatRegConnector.patchTurnoverEstimates(turnoverEstimates)
      shouldClear = turnoverEstimates.vatTaxable > 150000L
    } yield shouldClear

    frsShouldClear.flatMap {
      if (_) {
        frsService.clearFrs
          .flatMap(_ => frsService.saveJoiningFRS(answer = false))
          .map(_ => turnoverEstimates)
      }
      else {
        Future.successful(turnoverEstimates)
      }
    }
  }
}
