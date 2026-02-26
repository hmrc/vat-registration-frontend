/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.WorkersForm
import models.LabourCompliance
import play.api.mvc.{Action, AnyContent}
import services.{BusinessService, SessionProfile, SessionService, VatRegistrationService}
import views.html.sicandcompliance.Workers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WorkersController @Inject()(val authConnector: AuthClientConnector,
                                  val sessionService: SessionService,
                                  val businessService: BusinessService,
                                  val vatRegistrationService: VatRegistrationService,
                                  view: Workers)
                                 (implicit val appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          formFilled = businessDetails.labourCompliance.flatMap(_.numOfWorkersSupplied).fold(WorkersForm.form)(WorkersForm.form.fill)
        } yield Ok(view(formFilled))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          result <- {
            WorkersForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(view(badForm))),
              data => {
                val updatedLabourCompliance = businessDetails.labourCompliance
                  .getOrElse(LabourCompliance())
                  .copy(numOfWorkersSupplied = Some(data))

                businessService.updateBusiness(updatedLabourCompliance).flatMap { _ =>
                  Future.successful(Redirect(controllers.routes.TaskListController.show.url))
                }
              }
            )
          }
        } yield result
  }
}
