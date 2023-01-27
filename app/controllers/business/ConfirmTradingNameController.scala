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

package controllers.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ConfirmTradingNameForm
import models.api.{NETP, NonUkNonEstablished}
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.ConfirmTradingName
import services._
import views.html.business.ConfirmTradingNameView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConfirmTradingNameController @Inject()(val sessionService: SessionService,
                                             val authConnector: AuthClientConnector,
                                             val applicantDetailsService: ApplicantDetailsService,
                                             val businessService: BusinessService,
                                             val vatRegistrationService: VatRegistrationService,
                                             view: ConfirmTradingNameView)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val missingDataSection = "tasklist.aboutTheBusiness.heading"

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(
            throw MissingAnswerException(missingDataSection)
          ))
          business <- businessService.getBusiness
          form = business.hasTradingName.fold(ConfirmTradingNameForm.form)(ConfirmTradingNameForm.form.fill)
        } yield Ok(view(form, companyName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ConfirmTradingNameForm.form.bindFromRequest.fold(
          errors =>
            for {
              companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(
                throw MissingAnswerException(missingDataSection)
              ))
            } yield BadRequest(view(errors, companyName)),
          confirmTradingName => {
            for {
              _ <- businessService.updateBusiness(ConfirmTradingName(confirmTradingName))
              eligibilityData <- vatRegistrationService.getEligibilitySubmissionData
            } yield {
              if (confirmTradingName) {
                eligibilityData.partyType match {
                  case NETP | NonUkNonEstablished if !eligibilityData.fixedEstablishmentInManOrUk =>
                    Redirect(routes.InternationalPpobAddressController.show)
                  case _ =>
                    Redirect(routes.PpobAddressController.startJourney)
                }
              } else {
                Redirect(routes.CaptureTradingNameController.show)
              }
            }
          }
        )
  }

}
