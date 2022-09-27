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

package controllers.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.applicant.{routes => applicantRoutes}
import controllers.business.{routes => businessRoutes}
import forms.LeadPartnerForm
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, EntityService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.applicant.lead_partner_entity_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            entityService: EntityService,
                                            leadPartnerEntityPage: lead_partner_entity_type
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showLeadPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val form = entityService.getEntity(profile.registrationId, 1).map { entity =>
          LeadPartnerForm.form.fill(entity.partyType)
        } recoverWith {
          case _ => Future.successful(LeadPartnerForm.form)
        }

        form.map(f => Ok(leadPartnerEntityPage(f)))
  }

  def submitLeadPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        LeadPartnerForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(leadPartnerEntityPage(formWithErrors))),
          partyType =>
            for {
              _ <- entityService.upsertEntity[PartyType](profile.registrationId, 1, partyType)
            } yield partyType match {
              case Individual | NETP => Redirect(applicantRoutes.SoleTraderIdentificationController.startPartnerJourney)
              case UkCompany | RegSociety | CharitableOrg => Redirect(applicantRoutes.IncorpIdController.startPartnerJourney)
              case ScotPartnership => Redirect(businessRoutes.ScottishPartnershipNameController.show)
              case ScotLtdPartnership | LtdLiabilityPartnership => Redirect(applicantRoutes.PartnershipIdController.startPartnerJourney)
              case partyType => throw new InternalServerException(s"[LeadPartnerEntityController][submitLeadPartnerEntity] Submitted invalid lead partner: $partyType")
            }
        )
  }
}