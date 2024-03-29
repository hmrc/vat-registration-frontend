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
import forms.HasWebsiteForm.{form => hasWebsiteForm}
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.HasWebsiteAnswer
import services.{BusinessService, SessionService}
import views.html.business.HasWebsite

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasWebsiteController @Inject()(val authConnector: AuthClientConnector,
                                     val sessionService: SessionService,
                                     val businessService: BusinessService,
                                     view: HasWebsite)
                                    (implicit appConfig: FrontendAppConfig,
                                     val executionContext: ExecutionContext,
                                     baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          filledForm = businessDetails.hasWebsite.fold(hasWebsiteForm)(hasWebsiteForm.fill)
        } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        hasWebsiteForm.bindFromRequest.fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors))),
          hasWebsite =>
            businessService.updateBusiness(HasWebsiteAnswer(hasWebsite)) map { _ =>
              if (hasWebsite) {
                Redirect(controllers.business.routes.BusinessWebsiteAddressController.show)
              } else {
                Redirect(controllers.business.routes.VatCorrespondenceController.show)
              }
            }
        )
  }
}
