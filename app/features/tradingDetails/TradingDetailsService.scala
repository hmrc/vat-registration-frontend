/*
 * Copyright 2019 HM Revenue & Customs
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

package features.tradingDetails

import javax.inject.Inject
import connectors.RegistrationConnector
import models.{CurrentProfile, S4LKey}
import services._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TradingDetailsServiceImpl @Inject()(val s4lservice : S4LService,
                                          val regConnector : RegistrationConnector,
                                          val prePopService: PrePopService) extends TradingDetailsService {
  val s4lService: S4LService = s4lservice
  val registrationConnector : RegistrationConnector = regConnector
}

trait TradingDetailsService {
  val s4lService : S4LService
  val registrationConnector : RegistrationConnector
  val prePopService: PrePopService

  private val tradingDetailsS4LKey: S4LKey[TradingDetails] = S4LKey[TradingDetails]("tradingDetails")

  def getTradingDetailsViewModel(regId : String)(implicit hc : HeaderCarrier, profile : CurrentProfile): Future[TradingDetails] =
    s4lService.fetchAndGetNoAux(tradingDetailsS4LKey) flatMap {
      case Some(s4l) => Future.successful(s4l)
      case None => registrationConnector.getTradingDetails(regId) map {
        case Some(td) => td
        case None => TradingDetails()
      }
    }

  def getTradingNamePrepop(regId: String, tradingName: Option[TradingNameView])(implicit hc: HeaderCarrier): Future[Option[String]] = {
    if(tradingName.exists(_.yesNo)) Future.successful(None) else prePopService.getTradingName(regId)
  }

  def getS4LCompletion(data : TradingDetails) : Completion[TradingDetails] = data match {
    case TradingDetails(Some(_), Some(_)) => Complete(data)
    case _ => Incomplete(data)
  }

  def submitTradingDetails(regId : String, data : TradingDetails)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    getS4LCompletion(data).fold(
      incomplete  => s4lService.saveNoAux(incomplete, tradingDetailsS4LKey) map {
        _ => incomplete
      },
      { complete =>
        for {
          _ <- registrationConnector.upsertTradingDetails(regId, complete)
          _ <- s4lService.clear
        } yield complete
      }
    )
  }

  def updateTradingDetails(regId : String)(newS4L : (TradingDetails => TradingDetails))
                          (implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
    getTradingDetailsViewModel(regId) flatMap { storedData => submitTradingDetails(regId, newS4L(storedData))}
  }

  def saveTradingName(regId : String, hasName: Boolean, name : Option[String])
                     (implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[TradingDetails] = {
      updateTradingDetails(regId) {storedData => storedData.copy(tradingNameView = Some(TradingNameView(hasName, name)))}
        .flatMap{ tradingDetails =>  if(hasName && name.isDefined) {
          prePopService.saveTradingName(regId, name.get).map(_ => tradingDetails)
        } else {
          Future.successful(tradingDetails)
      }
     }
  }

  def saveEuGoods(regId : String, euGoods: Boolean)
                 (implicit hc : HeaderCarrier, currentProfile: CurrentProfile) : Future[TradingDetails] = {
    updateTradingDetails(regId) { storedData => storedData.copy(euGoods = Some(euGoods))}
  }
}
