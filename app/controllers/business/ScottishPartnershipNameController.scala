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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ScottishPartnershipNameForm
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.business.ScottishPartnershipName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScottishPartnershipNameController @Inject()(val sessionService: SessionService,
                                                  val authConnector: AuthClientConnector,
                                                  val entityService: EntityService,
                                                  val applicantDetailsService: ApplicantDetailsService,
                                                  val vatRegistrationService: VatRegistrationService,
                                                  view: ScottishPartnershipName)
                                                 (implicit appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext,
                                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          entity <- entityService.getEntity(profile.registrationId, 1)
        } yield entity.optScottishPartnershipName match {
          case Some(name) => Ok(view(ScottishPartnershipNameForm().fill(name)))
          case None => Ok(view(ScottishPartnershipNameForm()))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ScottishPartnershipNameForm.apply().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          companyName => {
            for {
              _ <- entityService.upsertEntity[String](profile.registrationId, 1, companyName)
            } yield {
              Redirect(controllers.grs.routes.PartnerPartnershipIdController.startPartnerJourney)
            }
          }
        )
  }
}
