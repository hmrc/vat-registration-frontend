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

package controllers

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.registration.applicant.{routes => applicantRoutes}
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, VatRegistrationService}
import views.html.honesty_declaration

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HonestyDeclarationController @Inject()(honestyDeclarationView: honesty_declaration,
                                             val authConnector: AuthClientConnector,
                                             val keystoreConnector: KeystoreConnector,
                                             val vatRegistrationService: VatRegistrationService
                                            )(implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      _ =>
        Future.successful(Ok(honestyDeclarationView(routes.HonestyDeclarationController.submit())))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          _ <- vatRegistrationService.submitHonestyDeclaration(regId = profile.registrationId, honestyDeclaration = true)
          partyType <- vatRegistrationService.partyType
        } yield {
          partyType match {
            case Individual =>
              Redirect(applicantRoutes.SoleTraderIdentificationController.startJourney())
            case UkCompany | RegSociety | CharitableOrg =>
              Redirect(applicantRoutes.IncorpIdController.startJourney())
            case Partnership =>
              Redirect(applicantRoutes.PartnershipIdController.startJourney())
            case UnincorpAssoc | Trust =>
              Redirect(applicantRoutes.BusinessIdController.startJourney())
          }
        }
  }
}
