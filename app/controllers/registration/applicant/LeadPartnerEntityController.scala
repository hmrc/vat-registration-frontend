/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import services.SessionService.leadPartnerEntityKey
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import forms.LeadPartnerForm
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, PartnersService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.lead_partner_entity_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            partnersService: PartnersService,
                                            leadPartnerEntityPage: lead_partner_entity_type
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showLeadPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        partnersService.getLeadPartner(profile.registrationId).map { optPartner =>
          val form = optPartner.map(_.partyType).fold(LeadPartnerForm.form)(LeadPartnerForm.form.fill(_))

          Ok(leadPartnerEntityPage(form))
        }
  }

  def submitLeadPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        LeadPartnerForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(leadPartnerEntityPage(formWithErrors))),
          partyType =>
            for {
              _ <- sessionService.cache[PartyType](leadPartnerEntityKey, partyType)
            } yield partyType match {
              case Individual | NETP => Redirect(applicantRoutes.SoleTraderIdentificationController.startPartnerJourney(true))
              case UkCompany | RegSociety | CharitableOrg => Redirect(applicantRoutes.IncorpIdController.startPartnerJourney)
              case _ => NotImplemented
            }
        )
  }


}
