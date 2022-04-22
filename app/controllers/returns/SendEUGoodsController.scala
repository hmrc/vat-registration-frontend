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

package controllers.returns

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.SendEuGoodsForm
import models.api.returns.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, ReturnsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.returns.SendEUGoodsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendEUGoodsController @Inject()(val authConnector: AuthConnector,
                                      val sessionService: SessionService,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      returnsService: ReturnsService,
                                      sendEUGoodsPage: SendEUGoodsView
                                     )(implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns.map { returns =>
          returns.overseasCompliance match {
            case Some(OverseasCompliance(_, Some(sendEuGoods), _, _, _, _)) => Ok(sendEUGoodsPage(SendEuGoodsForm.form.fill(sendEuGoods)))
            case _ => Ok(sendEUGoodsPage(SendEuGoodsForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SendEuGoodsForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(sendEUGoodsPage(badForm))),
          successForm => {
            for {
              returns <- returnsService.getReturns
              updatedReturns = returns.copy(
                overseasCompliance = returns.overseasCompliance.map(_.copy(
                  goodsToEu = Some(successForm)
                ))
              )
              _ <- returnsService.submitReturns(updatedReturns)
            } yield {
              Redirect(controllers.returns.routes.StoringGoodsController.show)
            }
          }
        )
  }
}