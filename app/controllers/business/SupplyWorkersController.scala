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

package controllers.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.SupplyWorkersForm
import models.LabourCompliance
import play.api.mvc.{Action, AnyContent}
import services.{BusinessService, SessionProfile, SessionService, VatRegistrationService}
import views.html.sicandcompliance.SupplyWorkers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SupplyWorkersController @Inject()(val authConnector: AuthClientConnector,
                                        val sessionService: SessionService,
                                        val businessService: BusinessService,
                                        val vatRegistrationService: VatRegistrationService,
                                        view: SupplyWorkers)
                                       (implicit val appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          supplyWorkers = businessDetails.labourCompliance.flatMap(_.supplyWorkers)
          formFilled = supplyWorkers.fold(SupplyWorkersForm.form)(SupplyWorkersForm.form.fill)
        } yield Ok(view(formFilled))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          result <- {
            SupplyWorkersForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(view(badForm))),
              data => {
                val withSupplyWorkers = businessDetails.labourCompliance.getOrElse(LabourCompliance()).copy(supplyWorkers = Some(data))
                val updatedLabourCompliance =
                  if (data) {
                    withSupplyWorkers.copy(intermediaryArrangement = None)
                  } else {
                    withSupplyWorkers.copy(numOfWorkersSupplied = None)
                  }
                businessService.updateBusiness(updatedLabourCompliance).map { _ =>
                  if (data) {
                    controllers.business.routes.WorkersController.show
                  } else {
                    controllers.business.routes.SupplyWorkersIntermediaryController.show
                  }
                } map Redirect
              }
          )}
        } yield result
  }
}
