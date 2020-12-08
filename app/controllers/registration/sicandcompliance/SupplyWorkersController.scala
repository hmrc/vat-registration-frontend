/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.KeystoreConnector
import controllers.BaseController
import forms.SupplyWorkersForm
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionProfile, SicAndComplianceService}
import views.html.labour.supply_workers

import scala.concurrent.{ExecutionContext, Future}

class SupplyWorkersController @Inject()(val authConnector: AuthClientConnector,
                                        val keystoreConnector: KeystoreConnector,
                                        val sicAndCompService: SicAndComplianceService,
                                        view: supply_workers)
                                       (implicit val appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          sicCompliance <- sicAndCompService.getSicAndCompliance
          formFilled = sicCompliance.supplyWorkers.fold(SupplyWorkersForm.form)(SupplyWorkersForm.form.fill)
        } yield Ok(view(formFilled))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SupplyWorkersForm.form.bindFromRequest().fold(
          badForm =>
            Future.successful(BadRequest(view(badForm))),
          view =>
            sicAndCompService.updateSicAndCompliance(view).map { _ =>
              if (view.yesNo) {
                controllers.registration.sicandcompliance.routes.WorkersController.show()
              } else {
                controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show()
              }
            } map Redirect
        )
  }

}
