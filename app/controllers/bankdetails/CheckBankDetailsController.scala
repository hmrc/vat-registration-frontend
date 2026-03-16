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
import models.BankAccountDetails
import models.bars.BankAccountDetailsSessionFormat
import play.api.Configuration
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService,LockService, SessionService}
import uk.gov.hmrc.crypto.SymmetricCryptoFactory
import views.html.bankdetails.CheckBankDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckBankDetailsController @Inject() (
    val authConnector: AuthClientConnector,
    val bankAccountDetailsService: BankAccountDetailsService,
    val sessionService: SessionService,
    val lockService: LockService,
    configuration: Configuration,
    view: CheckBankDetailsView
)(implicit appConfig: FrontendAppConfig, val executionContext: ExecutionContext, baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  private val encrypter =
    SymmetricCryptoFactory.aesCryptoFromConfig("json.encryption", configuration.underlying)

  private implicit val encryptedFormat: Format[BankAccountDetails] =
    BankAccountDetailsSessionFormat.format(encrypter)

  private val sessionKey = "bankAccountDetails"

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    if (isEnabled(UseNewBarsVerify)) {
      sessionService.fetchAndGet[BankAccountDetails](sessionKey).map {
        case Some(details) => Ok(view(details))
        case None          => Redirect(routes.HasBankAccountController.show)
      }
    } else {
      Future.successful(Redirect(routes.HasBankAccountController.show))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      for {
        details     <- sessionService.fetchAndGet[BankAccountDetails](sessionKey)
        bankAccount <- bankAccountDetailsService.getBankAccount
        result <- (details, bankAccount.flatMap(_.bankAccountType)) match {
          case (Some(accountDetails), Some(accountType)) =>
            bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails, Some(accountType)).map {
              case true  => Redirect(controllers.routes.TaskListController.show.url)
              case false =>
                lockService.incrementBarsAttempts(profile.registrationId).map { attempts =>
                  if (attempts >= appConfig.knownFactsLockAttemptLimit) {
                    Redirect(controllers.errors.routes.ThirdAttemptLockoutController.show)
                  } else {
                    Redirect(controllers.bankdetails.routes.AccountDetailsNotVerified.show)
                  }
                }
                Redirect(routes.UkBankAccountDetailsController.show)

            }
          case _ => Future.successful(Redirect(routes.HasBankAccountController.show))
        }
      } yield result
    } else {
      Future.successful(Redirect(routes.HasBankAccountController.show))
    }
  }
}
