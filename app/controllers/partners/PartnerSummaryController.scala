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
import forms.partners.PartnerSummaryForm
import play.api.mvc.{Action, AnyContent}
import services.AttachmentsService.AdditionalPartnersDocumentsAnswer
import services.{AttachmentsService, EntityService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.partners.PartnerSummary

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnerSummaryController @Inject()(val authConnector: AuthConnector,
                                         val sessionService: SessionService,
                                         entityService: EntityService,
                                         attachmentsService: AttachmentsService,
                                         view: PartnerSummary)
                                        (implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        entityService.getAllEntities(profile.registrationId).flatMap {
          case Nil => Future.successful(Redirect(controllers.routes.TaskListController.show))
          case partners =>
            val clearedPartners = partners.filter { partner =>
              partner.isModelComplete(isLeadPartner = partner.isLeadPartner.contains(true))
            }
            val page = Ok(view(PartnerSummaryForm(), clearedPartners, clearedPartners.size))

            if (clearedPartners != partners) {
              entityService.upsertEntityList(profile.registrationId, clearedPartners).map(_ => page)
            } else {
              Future.successful(page)
            }
        }
  }


  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        PartnerSummaryForm().bindFromRequest.fold(
          errors =>
            entityService.getAllEntities(profile.registrationId).map {
              case Nil => Redirect(controllers.routes.TaskListController.show)
              case partnerEntities => BadRequest(view(errors, partnerEntities, partnerEntities.size))
            },
          addMore =>
            if (addMore) {
              entityService.getNextValidIndex.flatMap { nextIndex =>
                if (nextIndex > appConfig.maxPartnerCount) {
                  attachmentsService.storeAttachmentDetails(profile.registrationId, AdditionalPartnersDocumentsAnswer(true)).map(_ => {
                    Redirect(routes.AdditionalPartnerEntityController.show)
                  })
                } else {
                  Future.successful(Redirect(routes.PartnerEntityTypeController.showPartnerType(nextIndex)))
                }
              }
            } else {
              attachmentsService.storeAttachmentDetails(profile.registrationId, AdditionalPartnersDocumentsAnswer(false)).map(_ => {
                Redirect(controllers.routes.TaskListController.show)
              })
            }
        )
  }

  def continue: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    Future.successful(Redirect(routes.PartnerEntityTypeController.showPartnerType(2)))
  }
}