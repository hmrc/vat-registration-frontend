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
import uk.gov.hmrc.http.InternalServerException
import views.html.applicant.BusinessPartnerEntityType

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnerEntityTypeController @Inject()(val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    val entityService: EntityService,
                                                    businessPartnerEntityTypePage: BusinessPartnerEntityType
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  implicit val errorKey: String = "pages.businessPartnerEntityType.missing"

  def showPartnerType(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.BusinessPartnerEntityTypeController.showPartnerType) {
          case Some(Entity(_, partyType, _, _, _, _, _)) =>
            Future.successful(Ok(businessPartnerEntityTypePage(PartnerForm.form.fill(partyType), isTransactor = true, index)))
          case None =>
            Future.successful(Ok(businessPartnerEntityTypePage(PartnerForm.form, isTransactor = true, index)))
        }
  }

  def submitPartnerType(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.BusinessPartnerEntityTypeController.showPartnerType) {
          for {
            result <- PartnerForm.form.bindFromRequest().fold(
              formWithErrors => Future.successful(BadRequest(businessPartnerEntityTypePage(formWithErrors, isTransactor = true, index))),
              partyType =>
                for {
                  _ <- entityService.upsertEntity[PartyType](profile.registrationId, index, partyType)
                } yield partyType match {
                  case UkCompany | RegSociety | CharitableOrg => Redirect(grsRoutes.PartnerIncorpIdController.startJourney(index))
                  case ScotPartnership => Redirect(routes.PartnerScottishPartnershipNameController.show(index))
                  case ScotLtdPartnership | LtdLiabilityPartnership => Redirect(grsRoutes.PartnerPartnershipIdController.startJourney(index))
                  case partyType => throw new InternalServerException(s"[BusinessLeadPartnerEntityController][submitPartnerEntity] Submitted invalid lead business partner: $partyType")
                }
            )
          } yield result
        }
  }
}