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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.TransactorTelephoneForm
import play.api.mvc.{Action, AnyContent}
import services.TransactorDetailsService.Telephone
import services.{SessionProfile, TransactorDetailsService}
import views.html.transactor.TelephoneNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TelephoneNumberController @Inject()(val keystoreConnector: KeystoreConnector,
                                          val authConnector: AuthClientConnector,
                                          val transactorDetailsService: TransactorDetailsService,
                                          view: TelephoneNumber)
                                         (implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          transactor <- transactorDetailsService.getTransactorDetails
          filledForm = transactor.telephone.fold(TransactorTelephoneForm.form)(TransactorTelephoneForm.form.fill)
        } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        TransactorTelephoneForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          phoneNumber =>
            transactorDetailsService.saveTransactorDetails(Telephone(phoneNumber)).map { _ =>
              NotImplemented
            }
        )
  }
}
