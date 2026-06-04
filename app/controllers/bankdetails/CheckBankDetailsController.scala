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

package controllers.bankdetails

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.bars.{BarsFailedNotLocked, BarsLockedOut, BarsSuccess}
import play.api.mvc.{Action, AnyContent}
import services.BankAccountDetailsService.redirectBackToFirstPageInJourney
import services.{BankAccountDetailsService, LockService, SessionService}
import views.html.bankdetails.CheckBankDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckBankDetailsController @Inject() (
    val authConnector: AuthClientConnector,
    val bankAccountDetailsService: BankAccountDetailsService,
    val sessionService: SessionService,
    val lockService: LockService,
    view: CheckBankDetailsView
)(implicit appConfig: FrontendAppConfig, val executionContext: ExecutionContext, baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    lockService.redirectIfBarsIsLocked {
      bankAccountDetailsService.getBankAccount.map(_.flatMap(_.details)).map {
        case Some(bankDetails) => Ok(view(bankDetails))
        case None              => Redirect(routes.CanYouProvideBankAccountDetailsController.show)
      }
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    bankAccountDetailsService.getBankAccount.flatMap { bankAccount =>
      (bankAccount.flatMap(_.details), bankAccount.flatMap(_.bankAccountType)) match {
        case (Some(bankAccountDetails), Some(accountType)) =>
          bankAccountDetailsService.verifyAndSaveBankAccountDetails(bankAccountDetails, accountType).map {
            case BarsSuccess         => Redirect(controllers.routes.TaskListController.show.url)
            case BarsFailedNotLocked => Redirect(routes.AccountDetailsNotVerifiedController.show)
            case BarsLockedOut       => Redirect(controllers.errors.routes.BankDetailsLockoutController.show)
          }
        case _ => Future.successful(redirectBackToFirstPageInJourney)
      }
    }
  }

}
