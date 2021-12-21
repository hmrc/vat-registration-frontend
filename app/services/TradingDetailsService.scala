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
import models.{CurrentProfile, TradingDetails, TradingNameView}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TradingDetailsService @Inject()(val s4lService: S4LService,
                                      val registrationConnector: VatRegistrationConnector,
                                      val prePopService: PrePopulationService) {

  def getTradingDetailsViewModel(regId: String)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[TradingDetails] =
    s4lService.fetchAndGet[TradingDetails] flatMap {
      case None | Some(TradingDetails(None, None, None)) => registrationConnector.getTradingDetails(regId) map {
        case Some(tradingDetails) => tradingDetails
        case None => TradingDetails()
      }
      case Some(tradingDetails) => Future.successful(tradingDetails)
    }

  def getS4LCompletion(data: TradingDetails): Completion[TradingDetails] = data match {
    case TradingDetails(Some(_), _, _) => Complete(data)
    case _ => Incomplete(data)
  }

  def submitTradingDetails(regId: String, data: TradingDetails)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    getS4LCompletion(data).fold(
      incomplete => s4lService.save(incomplete).map(_ => incomplete),
      complete => for {
        _ <- registrationConnector.upsertTradingDetails(regId, complete)
        _ <- s4lService.clearKey[TradingDetails]
      } yield complete
    )
  }

  def updateTradingDetails(regId: String)(newS4L: TradingDetails => TradingDetails)
                          (implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    getTradingDetailsViewModel(regId) flatMap { storedData => submitTradingDetails(regId, newS4L(storedData)) }
  }

  def saveTradingName(regId: String, hasName: Boolean, name: Option[String])
                     (implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    updateTradingDetails(regId) { storedData => storedData.copy(tradingNameView = Some(TradingNameView(hasName, name))) }
  }

  def saveEuGoods(regId: String, euGoods: Boolean)
                 (implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    updateTradingDetails(regId) {
      storedData => storedData.copy(euGoods = Some(euGoods))
    }
  }

  def saveShortOrgName(shortOrgName: String)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] =
    updateTradingDetails(currentProfile.registrationId) { tradingDetails =>
      tradingDetails.copy(shortOrgName = Some(shortOrgName))
    }
}
