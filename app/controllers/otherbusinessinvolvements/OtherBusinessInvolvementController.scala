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

package controllers.otherbusinessinvolvements

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.OtherBusinessInvolvementForm
import models.GroupRegistration
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.OtherBusinessInvolvementAnswer
import services._
import views.html.otherbusinessinvolvements.OtherBusinessInvolvement

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherBusinessInvolvementController @Inject()(val sessionService: SessionService,
                                                   val authConnector: AuthClientConnector,
                                                   businessService: BusinessService,
                                                   vatRegistrationService: VatRegistrationService,
                                                   view: OtherBusinessInvolvement)
                                                  (implicit appConfig: FrontendAppConfig,
                                                   val executionContext: ExecutionContext,
                                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          business <- businessService.getBusiness
          eligibilitySubmissionData <- vatRegistrationService.getEligibilitySubmissionData
          isVatGroup  = eligibilitySubmissionData.registrationReason.equals(GroupRegistration)
        } yield business.otherBusinessInvolvement match {
            case Some(answer) => Ok(view(OtherBusinessInvolvementForm.form.fill(answer), isVatGroup))
            case None => Ok(view(OtherBusinessInvolvementForm.form, isVatGroup))
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          eligibilitySubmissionData <- vatRegistrationService.getEligibilitySubmissionData
          isVatGroup  = eligibilitySubmissionData.registrationReason.equals(GroupRegistration)
          result <- {
            OtherBusinessInvolvementForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(view(errors, isVatGroup))),
          success => businessService.updateBusiness(OtherBusinessInvolvementAnswer(success)).flatMap { _ =>
            if (success) {
              Future.successful(Redirect(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1)))
            } else {
              Future.successful(Redirect(controllers.routes.TaskListController.show))
            }
          }
        )}
        } yield result
  }
}
