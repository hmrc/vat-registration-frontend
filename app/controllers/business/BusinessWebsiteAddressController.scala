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
import forms.BusinessWebsiteAddressForm
import play.api.mvc.{Action, AnyContent}
import services.BusinessContactService.Website
import services.{BusinessContactService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import views.html.business.BusinessWebsiteAddress

import scala.concurrent.{ExecutionContext, Future}

class BusinessWebsiteAddressController @Inject()(val sessionService: SessionService,
                                        val authConnector: AuthConnector,
                                        val businessContactService: BusinessContactService,
                                        view: BusinessWebsiteAddress)
                                       (implicit appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          business <- businessContactService.getBusinessContact
          form = business.website.fold(BusinessWebsiteAddressForm.form)(BusinessWebsiteAddressForm.form.fill)
        } yield Ok(view(routes.BusinessWebsiteAddressController.submit, form))


  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        BusinessWebsiteAddressForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(routes.BusinessWebsiteAddressController.submit, badForm))),
          businessWebsiteAddress =>
            businessContactService.updateBusinessContact(Website(businessWebsiteAddress)).map {
              _ => Redirect(controllers.business.routes.ContactPreferenceController.showContactPreference)
            }
        )
  }

}
