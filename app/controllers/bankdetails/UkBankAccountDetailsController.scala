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
import forms.{EnterBankAccountDetailsForm, EnterCompanyBankAccountDetailsForm}
import forms.EnterCompanyBankAccountDetailsForm.{form => enterBankAccountDetailsForm}
import models.BankAccountDetails
import models.bars.BankAccountDetailsSessionFormat
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import play.api.Configuration
import services.{BankAccountDetailsService,LockService, SessionService}
import uk.gov.hmrc.crypto.SymmetricCryptoFactory
import views.html.bankdetails.{EnterBankAccountDetails, EnterCompanyBankAccountDetails}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkBankAccountDetailsController @Inject() (
    val authConnector: AuthClientConnector,
    val bankAccountDetailsService: BankAccountDetailsService,
    val sessionService: SessionService,
    val lockService: LockService,
    configuration: Configuration,
    newBarsView: EnterBankAccountDetails,
    oldView: EnterCompanyBankAccountDetails
)(implicit appConfig: FrontendAppConfig, val executionContext: ExecutionContext, baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  private val encrypter =
    SymmetricCryptoFactory.aesCryptoFromConfig("json.encryption", configuration.underlying)

  private implicit val encryptedFormat: Format[BankAccountDetails] =
    BankAccountDetailsSessionFormat.format(encrypter)

  private val sessionKey = "bankAccountDetails"

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      lockService.getBarsAttemptsUsed(profile.registrationId).map(_ >= appConfig.knownFactsLockAttemptLimit).flatMap {
        case true => Future.successful(Redirect(controllers.errors.routes.ThirdAttemptLockoutController.show))
        case false =>
          val newBarsForm = EnterBankAccountDetailsForm.form
          sessionService.fetchAndGet[BankAccountDetails](sessionKey).map {
            case Some(details) => Ok(newBarsView(newBarsForm.fill(details)))
            case None => Ok(newBarsView(newBarsForm))
          }
      }
    }


    else {
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
            sessionService.cache[BankAccountDetails](sessionKey, accountDetails).map { _ =>
              Redirect(routes.CheckBankDetailsController.show)
            }
        )
    } else {
      enterBankAccountDetailsForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(oldView(formWithErrors))),
          accountDetails =>
            bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails, None).map {
              case true  => Redirect(controllers.routes.TaskListController.show.url)
              case false => BadRequest(oldView(EnterCompanyBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)))
            }
        )
    }
  }

}
