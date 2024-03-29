/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.BusinessTelephoneNumberForm
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.TelephoneNumber
import services.{BusinessService, SessionProfile, SessionService}
import views.html.business.BusinessTelephoneNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessTelephoneNumberController @Inject()(val sessionService: SessionService,
                                                  val authConnector: AuthClientConnector,
                                                  val businessService: BusinessService,
                                                  view: BusinessTelephoneNumber)
                                                 (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessContact <- businessService.getBusiness
          form = businessContact.telephoneNumber.fold(BusinessTelephoneNumberForm.form)(x => BusinessTelephoneNumberForm.form.fill(x))
        } yield Ok(view(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        BusinessTelephoneNumberForm.form.bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors))),
          telephoneNumber => businessService.updateBusiness[TelephoneNumber](TelephoneNumber(telephoneNumber)) map {
            _ => Redirect(controllers.business.routes.HasWebsiteController.show)
          }
        )
  }

}
