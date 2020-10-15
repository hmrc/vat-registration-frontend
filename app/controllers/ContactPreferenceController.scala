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
import forms.ContactPreferenceForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{BusinessContactService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.contact_preference

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactPreferenceController @Inject()(mcc: MessagesControllerComponents,
                                            val authConnector: AuthConnector,
                                            val keystoreConnector: KeystoreConnector,
                                            val businessContactService: BusinessContactService,
                                            view: contact_preference)
                                           (implicit val appConfig: FrontendAppConfig,
                                            ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def showContactPreference: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          contactPreference <- businessContactService.getBusinessContact
          form = contactPreference.contactPreference.fold(ContactPreferenceForm())(x => ContactPreferenceForm().fill(x))
        } yield Ok(view(form, routes.ContactPreferenceController.submitContactPreference()))

  }

  def submitContactPreference: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ContactPreferenceForm().bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm, routes.ContactPreferenceController.submitContactPreference()))),
          contactPreference =>
            businessContactService.updateBusinessContact(contactPreference).flatMap {
              _ =>
                Future.successful(Redirect(controllers.routes.SicAndComplianceController.showBusinessActivityDescription()))
            }
        )
  }

}
