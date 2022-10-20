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
import forms.SendEuGoodsForm
import models.api.vatapplication.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.GoodsToEu
import services.{ApplicantDetailsService, SessionProfile, SessionService, VatApplicationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.vatapplication.SendEUGoodsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendEUGoodsController @Inject()(val authConnector: AuthConnector,
                                      val sessionService: SessionService,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      vatApplicationService: VatApplicationService,
                                      sendEUGoodsPage: SendEUGoodsView
                                     )(implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          vatApplication.overseasCompliance match {
            case Some(OverseasCompliance(_, Some(sendEuGoods), _, _, _, _)) => Ok(sendEUGoodsPage(SendEuGoodsForm.form.fill(sendEuGoods)))
            case _ => Ok(sendEUGoodsPage(SendEuGoodsForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        SendEuGoodsForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(sendEUGoodsPage(badForm))),
          successForm => {
            vatApplicationService.saveVatApplication(GoodsToEu(successForm)).map { _ =>
              Redirect(controllers.vatapplication.routes.StoringGoodsController.show)
            }
          }
        )
  }
}
