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
import controllers.grs.{routes => grsRoutes}
import forms.PartnerForm
import models.Entity.leadEntityIndex
import models.api._
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.PartnerEntityType

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            entityService: EntityService,
                                            vatRegistrationService: VatRegistrationService,
                                            partnerEntityTypePage: PartnerEntityType
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showLeadPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          optEntity <- entityService.getEntity(profile.registrationId, leadEntityIndex)
          errorKey = if (isTransactor) "pages.leadPartnerEntityType.missing.3pt" else "pages.leadPartnerEntityType.missing"
          form = optEntity.fold(PartnerForm.form(errorKey))(entity =>
            if (isBusinessPartyType(entity.partyType)) {
              PartnerForm.form(errorKey).fill(BusinessEntity)
            } else {
              PartnerForm.form(errorKey).fill(entity.partyType)
            }
          )
        } yield Ok(partnerEntityTypePage(form, isTransactor, leadEntityIndex))
  }

  def submitLeadPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          errorKey = if (isTransactor) "pages.leadPartnerEntityType.missing.3pt" else "pages.leadPartnerEntityType.missing"
          result <- PartnerForm.form(errorKey).bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(partnerEntityTypePage(formWithErrors, isTransactor, leadEntityIndex))),
            {
              case partyType@Individual =>
                entityService.upsertEntity[PartyType](profile.registrationId, leadEntityIndex, partyType).map { _ =>
                  Redirect(grsRoutes.PartnerSoleTraderIdController.startJourney(leadEntityIndex))
                }
              case _ => Future.successful(Redirect(applicantRoutes.BusinessLeadPartnerEntityController.showPartnerEntityType))
            }
          )
        } yield result
  }

  private def isBusinessPartyType(partyType: PartyType) =
    List(NETP, UkCompany, RegSociety, CharitableOrg, ScotPartnership, ScotLtdPartnership, LtdLiabilityPartnership).contains(partyType)
}