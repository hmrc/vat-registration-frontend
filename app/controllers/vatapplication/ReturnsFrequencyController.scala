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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaskList
import forms.vatapplication.ReturnsFrequencyForm
import models.api.{NETP, NonUkNonEstablished}
import models.api.vatapplication.{Annual, Monthly}
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.vatapplication.return_frequency_view

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ReturnsFrequencyController @Inject()(val sessionService: SessionService,
                                           val authConnector: AuthClientConnector,
                                           val vatApplicationService: VatApplicationService,
                                           vatRegistrationService: VatRegistrationService,
                                           returnFrequencyPage: return_frequency_view
                                          )(implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          vatApplication <- vatApplicationService.getVatApplication
          showAAS <- vatApplicationService.isEligibleForAAS
          showMonthly = vatApplication.claimVatRefunds.contains(true)
        } yield {
          if (showAAS || showMonthly) {
            vatApplication.returnsFrequency match {
              case Some(frequency) => Ok(returnFrequencyPage(ReturnsFrequencyForm.form.fill(frequency), showAAS, showMonthly))
              case None => Ok(returnFrequencyPage(ReturnsFrequencyForm.form, showAAS, showMonthly))
            }
          } else {
            Redirect(routes.AccountingPeriodController.show)
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ReturnsFrequencyForm.form.bindFromRequest.fold(
          errors => for {
            vatApplication <- vatApplicationService.getVatApplication
            showAAS <- vatApplicationService.isEligibleForAAS
            showMonthly = vatApplication.claimVatRefunds.contains(true)
          } yield {
            BadRequest(returnFrequencyPage(errors, showAAS, showMonthly))
          },
          returnFrequency =>
            vatRegistrationService.partyType flatMap { partyType =>
              vatApplicationService.saveVatApplication(returnFrequency) map { _ =>
                returnFrequency match {
                  case Monthly =>
                    partyType match {
                      case NETP | NonUkNonEstablished => Redirect(controllers.vatapplication.routes.TaxRepController.show)
                      case _ =>
                        if (isEnabled(TaskList)) {
                          Redirect(controllers.routes.TaskListController.show)
                        } else {
                          Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show)
                        }
                    }
                  case Annual => Redirect(routes.LastMonthOfAccountingYearController.show)
                  case _ => Redirect(routes.AccountingPeriodController.show)
                }
            }
          }
        )
  }
}
