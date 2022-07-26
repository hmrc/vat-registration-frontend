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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaskList
import forms.VatExemptionForm
import models.NonUk
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.AppliedForExemption
import services.{SessionProfile, SessionService, VatApplicationService, VatRegistrationService}
import views.html.vatapplication.VatExemption

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatExemptionController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val vatApplicationService: VatApplicationService,
                                       val vatRegistrationService: VatRegistrationService,
                                       val vatExemptionView: VatExemption)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.appliedForExemption match {
            case Some(appliedForExemption) => Ok(vatExemptionView(VatExemptionForm.form.fill(appliedForExemption)))
            case None => Ok(vatExemptionView(VatExemptionForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        VatExemptionForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(vatExemptionView(errors))),
          success => {
            for {
              _ <- vatApplicationService.saveVatApplication(AppliedForExemption(success))
              registrationReason <- vatRegistrationService.getEligibilitySubmissionData.map(_.registrationReason)
            } yield registrationReason match {
              case NonUk =>
                Redirect(routes.SendGoodsOverseasController.show)
              case _ =>
                if (isEnabled(TaskList)) {
                  Redirect(controllers.routes.TaskListController.show.url)
                } else {
                  Redirect(controllers.bankdetails.routes.HasBankAccountController.show)
                }
            }
          }
        )
  }
}