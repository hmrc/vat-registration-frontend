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

package controllers.registration.annualAccountingScheme

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.AnnualAccountingSchemeForm
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthConnector
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import views.html.annualAccountingScheme.annual_accounting_scheme

@Singleton
class AnnualAccountingSchemeController @Inject()(val keystoreConnector: KeystoreConnector,
                                                 val authConnector: AuthConnector,
                                                 annualAccountingSchemeView: annual_accounting_scheme)
                                                (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(annualAccountingSchemeView(AnnualAccountingSchemeForm.form)))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
      AnnualAccountingSchemeForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(annualAccountingSchemeView(errors))),
        success => Future(Redirect(controllers.registration.annualAccountingScheme.routes.AnnualAccountingSchemeController.show()))
      )
  }

}
