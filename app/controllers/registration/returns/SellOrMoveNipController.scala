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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.SellOrMoveNipForm
import models.{ConditionalValue, NIPCompliance}
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService}
import views.html.returns.SellOrMoveNip

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SellOrMoveNipController @Inject()(val sessionService: SessionService,
                                        val authConnector: AuthClientConnector,
                                        val returnsService: ReturnsService,
                                        view: SellOrMoveNip)
                                       (implicit appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns.map { r =>
          r.northernIrelandProtocol match {
            case Some(NIPCompliance(Some(ConditionalValue(answer, amount)), _)) =>
              Ok(view(SellOrMoveNipForm.form.fill(answer, amount)))
            case _ => Ok(view(SellOrMoveNipForm.form))
          }
        }
  }
  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SellOrMoveNipForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          successForm => {
            val (goodsToEU, amount) = successForm
            for {
              r <- returnsService.getReturns
              updatedReturns = r.copy(
                northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(goodsToEU, amount)), r.northernIrelandProtocol.flatMap(_.goodsFromEU)))
              )
              _  <- returnsService.submitReturns(updatedReturns)
            } yield
              Redirect(controllers.registration.returns.routes.ReceiveGoodsNipController.show)
          }
        )
  }
}
