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

package controllers.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.business.{routes => businessRoutes}
import controllers.grs.{routes => grsRoutes}
import forms.PartnerForm
import models.Entity.leadEntityIndex
import models.api._
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.applicant.BusinessPartnerEntityType

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessLeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    val applicantDetailsService: ApplicantDetailsService,
                                                    entityService: EntityService,
                                                    vatRegistrationService: VatRegistrationService,
                                                    businessLeadPartnerEntityPage: BusinessPartnerEntityType
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          optEntity <- entityService.getEntity(profile.registrationId, leadEntityIndex)
          errorKey = if (isTransactor) "pages.businessLeadPartnerEntityType.missing.3pt" else "pages.businessLeadPartnerEntityType.missing"
          form = optEntity
            .fold(PartnerForm.form(errorKey))(entity =>
              PartnerForm.form(errorKey).fill(entity.partyType)
            )
        } yield Ok(businessLeadPartnerEntityPage(form, isTransactor, leadEntityIndex))
  }

  def submitPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          errorKey = if (isTransactor) "pages.businessLeadPartnerEntityType.missing.3pt" else "pages.businessLeadPartnerEntityType.missing"
          result <- PartnerForm.form(errorKey).bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(businessLeadPartnerEntityPage(formWithErrors, isTransactor, leadEntityIndex))),
            partyType =>
              for {
                _ <- entityService.upsertEntity[PartyType](profile.registrationId, leadEntityIndex, partyType)
              } yield partyType match {
                case UkCompany | RegSociety | CharitableOrg => Redirect(grsRoutes.PartnerIncorpIdController.startJourney(leadEntityIndex))
                case ScotPartnership => Redirect(businessRoutes.ScottishPartnershipNameController.show)
                case ScotLtdPartnership | LtdLiabilityPartnership => Redirect(grsRoutes.PartnerPartnershipIdController.startJourney(leadEntityIndex))
                case partyType => throw new InternalServerException(s"[BusinessLeadPartnerEntityController][submitPartnerEntity] Submitted invalid lead business partner: $partyType")
              }
          )
        } yield result
  }
}