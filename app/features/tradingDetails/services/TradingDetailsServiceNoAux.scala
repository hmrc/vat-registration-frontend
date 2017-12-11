/*
 * Copyright 2017 HM Revenue & Customs
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

package features.tradingDetails.services

import javax.inject.Inject

import connectors.RegistrationConnector
import features.tradingDetails.models._
import models.{CurrentProfile, S4LKey}
import services.S4LService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TradingDetailsServiceNoAux @Inject()(val s4lservice : S4LService,
                                           val regConnector : RegistrationConnector) extends TradingDetailsSrv {
  val s4lService: S4LService = s4lservice
  val registrationConnector : RegistrationConnector = regConnector
}

trait TradingDetailsSrv {
  val s4lService : S4LService
  val registrationConnector : RegistrationConnector

  private val tradingDetailsS4LKey: S4LKey[S4LTradingDetails] = S4LKey[S4LTradingDetails]("TradingDetails")

  def convertRegistrationModel(tradingDetails : Option[TradingDetails]) : S4LTradingDetails = tradingDetails match {
    case Some(td) => S4LTradingDetails(
      Some(TradingNameView(if (td.tradingName.isDefined) TradingNameView.TRADING_NAME_YES else TradingNameView.TRADING_NAME_NO,
        td.tradingName
      )),
      Some(EuGoodsView(td.eoriRequested.isDefined)),
      td.eoriRequested.map(eori => ApplyEoriView(eori))
    )
    case None => S4LTradingDetails()
  }

  def getTradingDetailsViewModel(regId : String)(implicit hc : HeaderCarrier, profile : CurrentProfile): Future[S4LTradingDetails] = {
    for {
      s4l <- s4lService.fetchAndGetNoAux(tradingDetailsS4LKey)
      model <- s4l match {
        case Some(s4lmodel) => Future.successful(s4lmodel)
        case None => for {
          api <- registrationConnector.getTradingDetails(regId)
        } yield {
          convertRegistrationModel(api)
        }
      }
    } yield {
      model
    }
  }

  def completedTradingDetails(data : S4LTradingDetails) : Either[S4LTradingDetails, TradingDetails] = data match {
    case S4LTradingDetails(Some(tn), Some(eu), Some(ae)) if eu.yesNo => Right(TradingDetails(tn.tradingName, Some(ae.yesNo)))
    case S4LTradingDetails(Some(tn), Some(eu), _) if !eu.yesNo => Right(TradingDetails(tn.tradingName, None))
    case _ => Left(data)
  }

  def submitTradingDetails(regId : String, data : S4LTradingDetails)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[Boolean] = {
    completedTradingDetails(data).fold(
      incomplete =>
        s4lService.saveNoAux(incomplete, tradingDetailsS4LKey) map {
          _ => true
        },
      complete =>
        for {
          _ <- registrationConnector.upsertTradingDetails(regId, complete)
          _ <- s4lService.clear
        } yield {
          true
        }
    )
  }

  def saveTradingName(regId : String, tradingNameView: TradingNameView)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[Boolean] = {
    getTradingDetailsViewModel(regId) flatMap {
      storedData => submitTradingDetails(regId, S4LTradingDetails(
        Some(tradingNameView), storedData.euGoodsView, storedData.applyEoriView
      ))
    }
  }

  def saveEuGoods(regId : String, euGoodsView: EuGoodsView)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile) : Future[Boolean] = {
    getTradingDetailsViewModel(regId) flatMap {
      storedData => submitTradingDetails(regId, S4LTradingDetails(
        storedData.tradingNameView, Some(euGoodsView), storedData.applyEoriView
      ))
    }
  }

  def saveEori(regId : String, applyEoriView: ApplyEoriView)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile) : Future[Boolean] = {
    getTradingDetailsViewModel(regId) flatMap {
      storedData => submitTradingDetails(regId, S4LTradingDetails(
        storedData.tradingNameView, storedData.euGoodsView, Some(applyEoriView)
      ))
    }
  }
}
