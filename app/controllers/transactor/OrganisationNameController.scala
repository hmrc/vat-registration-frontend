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

package controllers.transactor

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.OrganisationNameForm
import play.api.mvc.{Action, AnyContent}
import services.TransactorDetailsService._
import services.{SessionProfile, SessionService, TransactorDetailsService}
import views.html.transactor.OrganisationName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrganisationNameController @Inject()(val sessionService: SessionService,
                                           val authConnector: AuthClientConnector,
                                           val transactorDetailsService: TransactorDetailsService,
                                           view: OrganisationName)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          transactor <- transactorDetailsService.getTransactorDetails
          filledForm = transactor.organisationName.fold(OrganisationNameForm())(OrganisationNameForm().fill)
        } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        OrganisationNameForm().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          orgName =>
            transactorDetailsService.saveTransactorDetails(OrganisationName(orgName)).map { _ =>
              Redirect(routes.DeclarationCapacityController.show.url)
            }
        )
  }
}
