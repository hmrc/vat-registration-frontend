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

package controllers

import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnect
import features.tradingDetails.TradingDetailsService
import features.tradingDetails.views.html.{eori_apply => ApplyEoriPage, eu_goods => EuGoodsPage, trading_name => TradingNamePage}
import forms.{ApplyEoriForm, EuGoodsForm, TradingNameForm}
import play.api.i18n.MessagesApi
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class TradingDetailsControllerImpl @Inject()(val keystoreConnector: KeystoreConnect,
                                             val authConnector: AuthClientConnector,
                                             val tradingDetailsService: TradingDetailsService,
                                             val messagesApi: MessagesApi) extends TradingDetailsController {

}

trait TradingDetailsController extends BaseController with SessionProfile {

  val tradingDetailsService: TradingDetailsService
  val authConnector: AuthConnector
  val keystoreConnector: KeystoreConnect

  val tradingNamePage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.tradingNameView match {
            case Some(name) => Ok(TradingNamePage(TradingNameForm.form.fill(
              (name.yesNo, name.tradingName)
            )))
            case None => Ok(TradingNamePage(TradingNameForm.form))
          }
        }
      }
  }

  val submitTradingName = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        TradingNameForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(TradingNamePage(errors))),
          success => {
            val (hasName, name) = success
            tradingDetailsService.saveTradingName(profile.registrationId, hasName, name) map {
              _ => Redirect(controllers.routes.TradingDetailsController.euGoodsPage())
            }
          }
        )
      }
  }

  val euGoodsPage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.euGoods match {
            case Some(goods) => Ok(EuGoodsPage(EuGoodsForm.form.fill(goods)))
            case None => Ok(EuGoodsPage(EuGoodsForm.form))
          }
        }
      }
  }

  val submitEuGoods = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        EuGoodsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(EuGoodsPage(errors))),
          success => tradingDetailsService.saveEuGoods(profile.registrationId, success) map { _ =>
            if (success) {
              Redirect(controllers.routes.TradingDetailsController.applyEoriPage())
            } else {
              Redirect(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
            }
          }
        )
      }
  }

  val applyEoriPage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.applyEori match {
            case Some(eori) => Ok(ApplyEoriPage(ApplyEoriForm.form.fill(eori)))
            case None => Ok(ApplyEoriPage(ApplyEoriForm.form))
          }
        }
      }
  }

  val submitApplyEori = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        ApplyEoriForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(ApplyEoriPage(errors))),
          success => tradingDetailsService.saveEori(profile.registrationId, success) map {
            _ => Redirect(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
          }
        )
      }
  }
}
