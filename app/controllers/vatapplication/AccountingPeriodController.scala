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
import featureswitch.core.config.{TaskList, TaxRepPage}
import forms.vatapplication.AccountingPeriodForm
import models.api.vatapplication.QuarterlyStagger
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.vatapplication.AccountingPeriodView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountingPeriodController @Inject()(val sessionService: SessionService,
                                           val authConnector: AuthClientConnector,
                                           val vatApplicationService: VatApplicationService,
                                           accountingPeriodPage: AccountingPeriodView
                                          )(implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.staggerStart match {
            case Some(stagger: QuarterlyStagger) => Ok(accountingPeriodPage(AccountingPeriodForm.form.fill(stagger)))
            case _ => Ok(accountingPeriodPage(AccountingPeriodForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        AccountingPeriodForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(accountingPeriodPage(errors))),
          success => vatApplicationService.saveVatApplication(success) flatMap { _ =>
            if (isEnabled(TaxRepPage)) {
              Future.successful(Redirect(controllers.vatapplication.routes.TaxRepController.show))
            } else {
              if (isEnabled(TaskList)) {
                Future.successful(Redirect(controllers.routes.TaskListController.show))
              } else {
                Future.successful(Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show))
              }
            }
          }
        )
  }
}