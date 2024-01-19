/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.ZeroRated
import services.{SessionProfile, SessionService, VatApplicationService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ZeroRatedSuppliesResolverController @Inject()(val sessionService: SessionService,
                                                    val authConnector: AuthClientConnector,
                                                    vatApplicationService: VatApplicationService)
                                                   (implicit val executionContext: ExecutionContext,
                                                    appConfig: FrontendAppConfig,
                                                    baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  private val NoTurnover = BigDecimal("0")
  private val noZeroRatedSupplies = 0

  val missingDataSection = "tasklist.vatRegistration.goodsAndServices"

  def resolve: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getTurnover.flatMap {
          case Some(NoTurnover) =>
            vatApplicationService.saveVatApplication(ZeroRated(noZeroRatedSupplies)).map { _ =>
              Redirect(routes.SellOrMoveNipController.show)
            }
          case Some(_) =>
            Future.successful(Redirect(routes.ZeroRatedSuppliesController.show))
          case _ =>
            Future.failed(throw MissingAnswerException(missingDataSection))
        }
  }

}
