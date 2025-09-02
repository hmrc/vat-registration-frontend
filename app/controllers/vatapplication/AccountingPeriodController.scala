/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.vatapplication.AccountingPeriodForm
import models.api.vatapplication.QuarterlyStagger
import models.api.{Individual, LtdLiabilityPartnership, NETP, NonUkNonEstablished, Partnership, Trust}
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.vatapplication.AccountingPeriodView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountingPeriodController @Inject()(val sessionService: SessionService,
                                           val authConnector: AuthClientConnector,
                                           val vatApplicationService: VatApplicationService,
                                           vatRegistrationService: VatRegistrationService,
                                           accountingPeriodPage: AccountingPeriodView
                                          )(implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.staggerStart match {
            case Some(stagger: QuarterlyStagger) => Ok(accountingPeriodPage(AccountingPeriodForm.form.fill(stagger)))
            case _ => Ok(accountingPeriodPage(AccountingPeriodForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        AccountingPeriodForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(accountingPeriodPage(errors))),
          success =>
            vatRegistrationService.getEligibilitySubmissionData.flatMap { eligibilityData =>
              vatApplicationService.saveVatApplication(success) map { _ =>
                eligibilityData.partyType match {
                  case Individual | NonUkNonEstablished | Partnership | LtdLiabilityPartnership | Trust if !eligibilityData.fixedEstablishmentInManOrUk =>
                    Redirect(controllers.vatapplication.routes.TaxRepController.show)
                  case _ =>
                    Redirect(controllers.routes.TaskListController.show)
                }
            }
          }
        )
  }
}
