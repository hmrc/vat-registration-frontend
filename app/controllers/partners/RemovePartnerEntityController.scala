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

package controllers.partners

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.partners.RemovePartnerEntityForm
import models.Entity
import play.api.mvc.{Action, AnyContent}
import services.AttachmentsService.AdditionalPartnersDocumentsAnswer
import services.{AttachmentsService, EntityService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.partners.RemovePartnerEntity

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemovePartnerEntityController @Inject()(val authConnector: AuthConnector,
                                              val sessionService: SessionService,
                                              val entityService: EntityService,
                                              attachmentsService: AttachmentsService,
                                              view: RemovePartnerEntity)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with PartnerIndexValidation with SessionProfile {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.RemovePartnerEntityController.show) {
          case Some(entity: Entity) if entity.displayName.isDefined =>
            Future.successful(Ok(view(RemovePartnerEntityForm(entity.displayName).form, entity.displayName, index)))
          case _ =>
            logger.warn("[RemovePartnerEntityController] Attempted to remove partner entity without partner details")
            Future.successful(Redirect(routes.PartnerSummaryController.show))
        }
  }

  def submit(partnerName: Option[String], index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.RemovePartnerEntityController.show) {
          RemovePartnerEntityForm(partnerName).form.bindFromRequest.fold(
            errors =>
              Future.successful(BadRequest(view(errors, partnerName, index))),
            success => {
              if (success) {
                for {
                  nextIndex <- entityService.getNextValidIndex
                  _ <- entityService.deleteEntity(profile.registrationId, index)
                  _ <-
                    if (nextIndex > appConfig.maxPartnerCount) {
                      attachmentsService.storeAttachmentDetails(profile.registrationId, AdditionalPartnersDocumentsAnswer(false))
                    } else {
                      Future.successful(true)
                    }
                } yield Redirect(routes.PartnerSummaryController.show)
              } else {
                Future.successful(Redirect(routes.PartnerSummaryController.show))
              }
            }
          )
        }
  }
}
