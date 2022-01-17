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

package controllers.registration.flatratescheme

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import models.GroupRegistration
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException
import views.html.frs_join

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JoinFlatRateSchemeController @Inject()(val flatRateService: FlatRateService,
                                             val vatRegistrationService: VatRegistrationService,
                                             val authConnector: AuthClientConnector,
                                             val sessionService: SessionService,
                                             view: frs_join)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form()("frs.join")

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          turnoverEstimates <- vatRegistrationService.fetchTurnoverEstimates.map(_.getOrElse(throw new InternalServerException("[JoinFRSController][show] Missing turnover estimates")))
          isGroupRegistration <- vatRegistrationService.getEligibilitySubmissionData.map(_.registrationReason.equals(GroupRegistration))
          redirect <-
            if (turnoverEstimates.turnoverEstimate > 150000L || isGroupRegistration) {
              Future.successful(Redirect(controllers.registration.attachments.routes.DocumentsRequiredController.resolve))
            } else {
              flatRateService.getFlatRate.map { flatRateScheme =>
                val form = flatRateScheme.joinFrs.fold(joinFrsForm)(v => joinFrsForm.fill(YesOrNoAnswer(v)))
                Ok(view(form))
              }
            }
        } yield {
          redirect
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        joinFrsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          joiningFRS => flatRateService.saveJoiningFRS(joiningFRS.answer) map { _ =>
            if (joiningFRS.answer) {
              Redirect(controllers.routes.FlatRateController.annualCostsInclusivePage)
            } else {
              Redirect(controllers.registration.attachments.routes.DocumentsRequiredController.resolve)
            }
          }
        )
  }

}
