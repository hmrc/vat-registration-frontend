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

package controllers.business

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.BusinessEmailAddressForm
import play.api.mvc.{Action, AnyContent}
import services.BusinessContactService.Email
import services.{BusinessContactService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import views.html.business.BusinessEmail

import scala.concurrent.{ExecutionContext, Future}

class BusinessEmailController @Inject()(val sessionService: SessionService,
                                        val authConnector: AuthConnector,
                                        val businessContactService: BusinessContactService,
                                        view: BusinessEmail)
                                       (implicit appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          business <- businessContactService.getBusinessContact
          form = business.email.fold(BusinessEmailAddressForm.form)(BusinessEmailAddressForm.form.fill)
        } yield Ok(view(routes.BusinessEmailController.submit, form))


  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        BusinessEmailAddressForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(routes.BusinessEmailController.submit, badForm))),
          businessEmail =>
            businessContactService.updateBusinessContact(Email(businessEmail)).flatMap {
              _ =>
              Future.successful(NotImplemented)
            }

        )
  }

}
