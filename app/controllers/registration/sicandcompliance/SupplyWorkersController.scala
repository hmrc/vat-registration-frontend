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

package controllers.registration.sicandcompliance

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.SupplyWorkersForm
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, SicAndComplianceService, VatRegistrationService}
import views.html.labour.supply_workers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SupplyWorkersController @Inject()(val authConnector: AuthClientConnector,
                                        val sessionService: SessionService,
                                        val sicAndCompService: SicAndComplianceService,
                                        val vatRegistrationService: VatRegistrationService,
                                        view: supply_workers)
                                       (implicit val appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          sicCompliance <- sicAndCompService.getSicAndCompliance
          isTransactor <- vatRegistrationService.isTransactor
          formFilled = sicCompliance.supplyWorkers.fold(SupplyWorkersForm.form(isTransactor))(SupplyWorkersForm.form(isTransactor).fill)
        } yield Ok(view(formFilled, isTransactor))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          result <- {
            SupplyWorkersForm.form(isTransactor).bindFromRequest().fold(
            badForm =>
              vatRegistrationService.isTransactor.map { _ =>
                BadRequest(view(badForm, isTransactor))
              },
            view =>
              sicAndCompService.updateSicAndCompliance(view).map { _ =>
                if (view.yesNo) {
                  controllers.registration.sicandcompliance.routes.WorkersController.show
                } else {
                  controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show
                }
              } map Redirect
          )}
        } yield result
  }
}
