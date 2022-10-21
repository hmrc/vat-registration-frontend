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
import controllers.grs.{routes => grsRoutes}
import forms.PartnerForm
import models.Entity
import models.api._
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.PartnerEntityType

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerEntityTypeController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val entityService: EntityService,
                                            partnerEntityTypePage: PartnerEntityType
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  implicit val errorKey: String = "pages.partnerEntityType.missing"

  def showPartnerType(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.PartnerEntityTypeController.showPartnerType) {
          case Some(Entity(_, partyType, _, _, _, _, _)) =>
            val form = if (isBusinessPartyType(partyType)) {
              PartnerForm.form.fill(BusinessEntity)
            } else {
              PartnerForm.form.fill(partyType)
            }
            Future.successful(Ok(partnerEntityTypePage(form, isTransactor = true, index)))
          case None =>
            Future.successful(Ok(partnerEntityTypePage(PartnerForm.form, isTransactor = true, index)))
        }

  }

  def submitPartnerType(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerEntityTypeController.showPartnerType) {
          for {
            result <- PartnerForm.form.bindFromRequest().fold(
              formWithErrors => Future.successful(BadRequest(partnerEntityTypePage(formWithErrors, isTransactor = true, index))),
              {
                case Individual =>
                  entityService.upsertEntity[PartyType](profile.registrationId, index, Individual).map { _ =>
                    Redirect(grsRoutes.PartnerSoleTraderIdController.startJourney(index))
                  }
                case _@BusinessEntity =>
                  Future.successful(Redirect(controllers.partners.routes.BusinessPartnerEntityTypeController.showPartnerType(index)))
                case partyType =>
                  Future.successful(InternalServerError(s"[PartnerEntityTypeController] Invalid party type submitted, $partyType"))
              }
            )
          } yield result
        }
  }

  private def isBusinessPartyType(partyType: PartyType) =
    List(NETP, UkCompany, RegSociety, CharitableOrg, ScotPartnership, ScotLtdPartnership, LtdLiabilityPartnership).contains(partyType)
}