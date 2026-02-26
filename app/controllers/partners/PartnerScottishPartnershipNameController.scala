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

package controllers.partners

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.PartnerScottishPartnershipNameForm
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.partners.PartnerScottishPartnershipName
import services.EntityService.ScottishPartnershipName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnerScottishPartnershipNameController @Inject()(val sessionService: SessionService,
                                                         val authConnector: AuthClientConnector,
                                                         val entityService: EntityService,
                                                         val applicantDetailsService: ApplicantDetailsService,
                                                         val vatRegistrationService: VatRegistrationService,
                                                         view: PartnerScottishPartnershipName)
                                                        (implicit appConfig: FrontendAppConfig,
                                                         val executionContext: ExecutionContext,
                                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.PartnerScottishPartnershipNameController.show) { optEntity =>
          val form = optEntity.flatMap(_.optScottishPartnershipName)
            .fold(PartnerScottishPartnershipNameForm())(PartnerScottishPartnershipNameForm().fill)

          Future.successful(Ok(view(form, index)))
        }
  }

  def submit(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerScottishPartnershipNameController.show) {
          PartnerScottishPartnershipNameForm.apply().bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
            companyName => {
              entityService.upsertEntity[ScottishPartnershipName](profile.registrationId, index, ScottishPartnershipName(companyName)).map { _ =>
                Redirect(controllers.grs.routes.PartnerPartnershipIdController.startJourney(index))
              }
            }
          )
        }
  }
}
