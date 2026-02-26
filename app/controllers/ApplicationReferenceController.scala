/*
 * Copyright 2026 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.RegistrationApiConnector.applicationReferenceKey
import forms.ApplicationReferenceForm
import models.ApiKey
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.ApplicationReference

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationReferenceController @Inject()(val authConnector: AuthConnector,
                                               val sessionService: SessionService,
                                               vatRegistrationService: VatRegistrationService,
                                               view: ApplicationReference,
                                               form: ApplicationReferenceForm)
                                              (implicit val executionContext: ExecutionContext,
                                               bcc: BaseControllerComponents,
                                               appConfig: FrontendAppConfig) extends BaseController with SessionProfile {


  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    implicit val key: ApiKey[String] = applicationReferenceKey

    vatRegistrationService.getSection[String](profile.registrationId)
      .map {
        case Some(appRef) => Ok(view(form().fill(appRef)))
        case _ => Ok(view(form()))
      }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      appRef => {
        implicit val key: ApiKey[String] = applicationReferenceKey

        for {
          _ <- vatRegistrationService.upsertSection[String](profile.registrationId, appRef)
          _ <- vatRegistrationService.raiseAuditEvent
        } yield Redirect(routes.HonestyDeclarationController.show)
      }
    )
  }

}
