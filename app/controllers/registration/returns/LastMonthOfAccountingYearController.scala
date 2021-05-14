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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.AnnualStaggerForm
import models.api.returns.FebJanStagger
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import views.html.returns.last_month_of_accounting_year

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LastMonthOfAccountingYearController @Inject()(view: last_month_of_accounting_year,
                                                    val authConnector: AuthClientConnector,
                                                    val keystoreConnector: KeystoreConnector
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      _ =>
        Future.successful(Ok(view(AnnualStaggerForm.form.fill(FebJanStagger)))) //TODO Fill form when data storage is done
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        AnnualStaggerForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          annualStagger => Future.successful(Redirect(routes.LastMonthOfAccountingYearController.submit())) //TODO Store data and do correct redirect
        )
  }
}
