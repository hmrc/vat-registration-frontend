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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ContactPreferenceForm
import play.api.mvc.{Action, AnyContent}
import services.{BusinessService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.business.ContactPreferenceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactPreferenceController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val businessService: BusinessService,
                                            view: ContactPreferenceView)
                                           (implicit val appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showContactPreference: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          contactPreference <- businessService.getBusiness
          form = contactPreference.contactPreference.fold(ContactPreferenceForm())(ContactPreferenceForm().fill)
        } yield Ok(view(form, routes.ContactPreferenceController.submitContactPreference))

  }

  def submitContactPreference: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ContactPreferenceForm().bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm, routes.ContactPreferenceController.submitContactPreference))),
          contactPreference =>
            businessService.updateBusiness(contactPreference).map { _ =>
              Redirect(controllers.routes.TaskListController.show)
            }
        )
  }

}
