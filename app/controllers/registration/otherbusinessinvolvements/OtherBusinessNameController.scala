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
import forms.otherbusinessinvolvements.OtherBusinessNameForm
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.OtherBusinessInvolvementsService.BusinessNameAnswer
import services.{OtherBusinessInvolvementsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.otherbusinessinvolvements.OtherBusinessName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherBusinessNameController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            otherBusinessInvolvementsService: OtherBusinessInvolvementsService,
                                            view: OtherBusinessName)
                                           (implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with ObiIndexValidation with SessionProfile {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.OtherBusinessNameController.show) {
          otherBusinessInvolvementsService.getOtherBusinessInvolvement(index).flatMap {
            case Some(OtherBusinessInvolvement(Some(businessName), _, _, _, _, _)) =>
              Future.successful(Ok(view(OtherBusinessNameForm().fill(businessName), index)))
            case Some(_) =>
              Future.successful(Ok(view(OtherBusinessNameForm(), index)))
            case None =>
              otherBusinessInvolvementsService.getHighestValidIndex.map { maxIndex =>
                if (index > maxIndex) {
                  Redirect(routes.OtherBusinessNameController.show(maxIndex))
                } else {
                  Ok(view(OtherBusinessNameForm(), index))
                }
              }
          }
        }
  }

  def submit(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        OtherBusinessNameForm().bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors, index))),
          success => {
            otherBusinessInvolvementsService.updateOtherBusinessInvolvement(index, BusinessNameAnswer(success)).map { _ =>
              Redirect(routes.OtherBusinessNameController.show(index)) //TODO Route to next page when it is done
            }
          }
        )
  }

}
