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

package controllers.registration.otherbusinessinvolvements

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.{OtherBusinessInvolvementsService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.otherbusinessinvolvements.OtherBusinessCheckAnswers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherBusinessCheckAnswersController @Inject()(val bcc: BaseControllerComponents,
                                                    val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    obiService: OtherBusinessInvolvementsService,
                                                    view: OtherBusinessCheckAnswers)
                                                   (implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents) extends BaseController with ObiIndexValidation {

  def show(index: Int, changeMode: Boolean): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>implicit profile =>
    validateIndex(index, idx => routes.OtherBusinessCheckAnswersController.show(idx)) {
      obiService.getOtherBusinessInvolvement(index).flatMap {
        case Some(OtherBusinessInvolvement(Some(businessName), Some(hasVrn), optVrn, optHasUtr, optUtr, Some(stillTrading))) =>
          Future.successful(Ok(view(index, businessName, hasVrn, optVrn, optHasUtr, optUtr, stillTrading, changeMode)))
        case _ =>
          obiService.getHighestValidIndex.map { maxIndex =>
            if (index > maxIndex) {
              Redirect(routes.OtherBusinessCheckAnswersController.show(maxIndex))
            } else {
              // If data doesn't exist, allow user to provide it
              Redirect(routes.OtherBusinessNameController.show(index))
            }
          }
      }
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    Future.successful(NotImplemented) /* TODO: Redirect to OBI summary page */
  }

}
