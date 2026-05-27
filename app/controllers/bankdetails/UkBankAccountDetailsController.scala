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
import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport.isEnabled
import forms.EnterCompanyBankAccountDetailsForm.{form => enterBankAccountDetailsForm}
import forms.{EnterBankAccountDetailsForm, EnterCompanyBankAccountDetailsForm}
import play.api.mvc.{Action, AnyContent}
import services.BankAccountDetailsService.redirectBackToFirstPageInJourney
import services.{BankAccountDetailsService, LockService, SessionService}
import views.html.bankdetails.{EnterBankAccountDetails, EnterCompanyBankAccountDetails}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkBankAccountDetailsController @Inject() (
    val authConnector: AuthClientConnector,
    val bankAccountDetailsService: BankAccountDetailsService,
    val sessionService: SessionService,
    val lockService: LockService,
    newBarsView: EnterBankAccountDetails,
    oldView: EnterCompanyBankAccountDetails
)(implicit appConfig: FrontendAppConfig, val executionContext: ExecutionContext, baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      lockService.redirectIfBarsIsLocked {
        val newBarsForm = EnterBankAccountDetailsForm.form
        bankAccountDetailsService.getBankAccount.map { bankAccount =>
          bankAccount.flatMap(_.details) match {
            case Some(details) => Ok(newBarsView(newBarsForm.fill(details)))
            case None          => Ok(newBarsView(newBarsForm))
          }
        }
      }
    } else {
      for {
        bankDetails <- bankAccountDetailsService.getBankAccount
        filledForm = bankDetails.flatMap(_.details).fold(enterBankAccountDetailsForm)(enterBankAccountDetailsForm.fill)
      } yield Ok(oldView(filledForm))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      val newBarsForm = EnterBankAccountDetailsForm.form
      newBarsForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(newBarsView(formWithErrors))),
          accountDetails =>
            bankAccountDetailsService.saveAnswersForBankAccountDetailsPage(accountDetails).map {
              case Right(_) =>
                Redirect(routes.CheckBankDetailsController.show)
              case Left(_) =>
                redirectBackToFirstPageInJourney
            }
        )
    } else {
      enterBankAccountDetailsForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(oldView(formWithErrors))),
          accountDetails =>
            bankAccountDetailsService.makeBarsValidationCheckAndSaveValidAnswers(accountDetails).map { barsWasSuccessful =>
              if (barsWasSuccessful) {
                Redirect(controllers.routes.TaskListController.show.url)
              } else {
                BadRequest(oldView(EnterCompanyBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)))
              }
            }
        )
    }
  }

}
