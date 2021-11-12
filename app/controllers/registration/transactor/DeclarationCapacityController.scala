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

package controllers.registration.transactor

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.DeclarationCapacityForm
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, TransactorDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.transactor.DeclarationCapacityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationCapacityController @Inject()(view: DeclarationCapacityView,
                                              val authConnector: AuthConnector,
                                              val keystoreConnector: KeystoreConnector,
                                              val transactorDetailsService: TransactorDetailsService)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          transactor <- transactorDetailsService.getTransactorDetails
          filledForm = transactor.declarationCapacity.fold(DeclarationCapacityForm())(DeclarationCapacityForm().fill)
        } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        DeclarationCapacityForm().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          declarationCapacity =>
            transactorDetailsService.saveTransactorDetails(declarationCapacity).map { _ =>
              Redirect(routes.TransactorIdentificationController.startJourney().url)
            }
        )
  }
}
