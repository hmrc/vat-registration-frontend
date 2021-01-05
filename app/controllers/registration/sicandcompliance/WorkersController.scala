/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.WorkersForm
import javax.inject.Inject
import models.SicAndCompliance
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionProfile, SicAndComplianceService}
import views.html.labour.workers

import scala.concurrent.{ExecutionContext, Future}

class WorkersController @Inject()(val authConnector: AuthClientConnector,
                                  val keystoreConnector: KeystoreConnector,
                                  val sicAndCompService: SicAndComplianceService,
                                  view: workers)
                                 (implicit val appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          sicCompliance <- sicAndCompService.getSicAndCompliance
          formFilled = sicCompliance.workers.fold(WorkersForm.form)(WorkersForm.form.fill)
        } yield Ok(view(formFilled))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        WorkersForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          data => sicAndCompService.updateSicAndCompliance(data) map { _ =>
            Redirect(controllers.registration.business.routes.TradingNameController.show())
          }
        )
  }

}
