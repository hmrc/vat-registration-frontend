/*
 * Copyright 2023 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.PartnerTelephoneForm
import play.api.mvc.{Action, AnyContent}
import services.EntityService.Telephone
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.partners.PartnerTelephoneNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnerTelephoneNumberController @Inject()(val sessionService: SessionService,
                                                 val authConnector: AuthConnector,
                                                 val entityService: EntityService,
                                                 view: PartnerTelephoneNumber)
                                                (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.PartnerTelephoneNumberController.show) { optEntity =>
          val form = optEntity.flatMap(_.telephoneNumber)
            .fold(PartnerTelephoneForm.form)(PartnerTelephoneForm.form.fill)
          optEntity match {
            case Some(entity) if entity.displayName.isDefined =>
              Future.successful(Ok(view(form, index, entity.displayName)))
            case _ =>
              logger.warn("[PartnerTelephoneNumberController] Attempted to capture telephone number without partyType")
              Future.successful(Redirect(routes.PartnerEntityTypeController.showPartnerType(index)))
          }
        }
  }

  def submit(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerTelephoneNumberController.show) {
          PartnerTelephoneForm.form.bindFromRequest().fold(
            formWithErrors =>
              entityService.getEntity(profile.registrationId, index).flatMap {
                case Some(entity) if entity.displayName.isDefined =>
                  Future.successful(BadRequest(view(formWithErrors, index, entity.displayName)))
                case _ =>
                  Future.successful(BadRequest(view(formWithErrors, index, None)))
              },
            telephoneNumber => {
              entityService.upsertEntity[Telephone](profile.registrationId, index, Telephone(telephoneNumber)).map { _ =>
                Redirect(routes.PartnerCaptureEmailAddressController.show(index))
              }
            }
          )
        }
  }

}
