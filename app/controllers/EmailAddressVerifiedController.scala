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

package controllers

import config.FrontendAppConfig
import connectors.KeystoreConnector
import forms.EmailPasscodeForm
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.email_verified

import scala.concurrent.{ExecutionContext, Future}

class EmailAddressVerifiedController @Inject()(view: email_verified,
                                               mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnector
                                              )(implicit val appConfig: FrontendAppConfig,
                                                ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(view(routes.EmailAddressVerifiedController.submit())))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Future.successful(Redirect(routes.BusinessContactDetailsController.showPPOB()))
  }

}