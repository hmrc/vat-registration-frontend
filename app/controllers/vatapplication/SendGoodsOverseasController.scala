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

package controllers.vatapplication

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.SendGoodsOverseasForm
import models.api.vatapplication.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.GoodsToOverseas
import services.{SessionProfile, SessionService, VatApplicationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.vatapplication.SendGoodsOverseasView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendGoodsOverseasController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val vatApplicationService: VatApplicationService,
                                            val view: SendGoodsOverseasView)
                                           (implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          vatApplication.overseasCompliance match {
            case Some(OverseasCompliance(Some(goodsToOverseas), _, _, _, _, _)) =>
              Ok(view(SendGoodsOverseasForm.form.fill(goodsToOverseas)))
            case _ =>
              Ok(view(SendGoodsOverseasForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SendGoodsOverseasForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(view(errors))),
          success => {
            vatApplicationService.saveVatApplication(GoodsToOverseas(success)).map { _ =>
              if (success) {
                Redirect(routes.SendEUGoodsController.show)
              } else {
                Redirect(routes.StoringGoodsController.show)
              }
            }
          }
        )
  }
}
