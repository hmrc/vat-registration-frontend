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
import controllers.business.{routes => businessRoutes}
import controllers.grs.{routes => grsRoutes}
import forms.LeadPartnerForm
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, EntityService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.applicant.partnership_business_entity_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessLeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    val applicantDetailsService: ApplicantDetailsService,
                                                    entityService: EntityService,
                                                    vatRegistrationService: VatRegistrationService,
                                                    businessLeadPartnerEntityPage: partnership_business_entity_type
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  implicit val errorKey: String = "pages.businessLeadPartnerEntityType.missing"

  def showPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          form <- entityService.getEntity(profile.registrationId, 1).map(entity => {
            LeadPartnerForm.form.fill(entity.partyType)
          }) recoverWith {
            case _ => Future.successful(LeadPartnerForm.form)
          }
        } yield Ok(businessLeadPartnerEntityPage(form, isTransactor))
  }

  def submitPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          result <- LeadPartnerForm.form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(businessLeadPartnerEntityPage(formWithErrors, isTransactor))),
            partyType =>
              for {
                _ <- entityService.upsertEntity[PartyType](profile.registrationId, 1, partyType)
              } yield partyType match {
                case UkCompany | RegSociety | CharitableOrg => Redirect(grsRoutes.PartnerIncorpIdController.startPartnerJourney)
                case ScotPartnership => Redirect(businessRoutes.ScottishPartnershipNameController.show)
                case ScotLtdPartnership | LtdLiabilityPartnership => Redirect(grsRoutes.PartnerPartnershipIdController.startPartnerJourney)
                case partyType => throw new InternalServerException(s"[BusinessLeadPartnerEntityController][submitPartnerEntity] Submitted invalid lead business partner: $partyType")
              }
          )
        } yield result
  }
}