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

import config.AuthClientConnector
import connectors.KeystoreConnector
import controllers.BaseController
import forms.EstimateVatTurnoverForm
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile

import scala.concurrent.Future

class TurnoverEstimatesControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                val authConnector: AuthClientConnector,
                                                val keystoreConnector: KeystoreConnector,
                                                val service: TurnoverEstimatesService) extends TurnoverEstimatesController {

}

trait TurnoverEstimatesController extends BaseController with SessionProfile {

  val service: TurnoverEstimatesService

  private val estimateVatTurnoverForm = EstimateVatTurnoverForm.form

  def showEstimateVatTurnover: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        service.fetchTurnoverEstimates map { turnover =>
          val form = turnover match {
            case Some(turnoverEstimates) => estimateVatTurnoverForm.fill(turnoverEstimates.vatTaxable)
            case None                    => estimateVatTurnoverForm
          }
          Ok(views.html.estimate_vat_turnover(form))
        }
      }
  }

  def submitEstimateVatTurnover: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        estimateVatTurnoverForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.estimate_vat_turnover(errors))),
          estimatedVatTurnover =>
            service.saveTurnoverEstimates(TurnoverEstimates(estimatedVatTurnover)) map { _ =>
              Redirect(features.returns.controllers.routes.ReturnsController.chargeExpectancyPage())
            }
        )
      }
  }
}
