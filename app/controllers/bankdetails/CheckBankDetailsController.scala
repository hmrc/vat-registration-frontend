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
import models.{BankAccountDetails, DontWantToProvide}
import models.bars.{BankAccountDetailsSessionFormat, BarsFailedNotLocked, BarsLockedOut, BarsSuccess}
import play.api.Configuration
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, LockService, SessionService}
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

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      lockService.redirectIfBarsIsLocked {
        for {
          bankDetails <- sessionService.fetchAndGet[BankAccountDetails](sessionKey)
          fromEnter <- sessionService.fetchAndGet[Boolean]("fromEnterDetails")
          _ <-
            if (fromEnter.contains(true)) sessionService.cache[Boolean]("fromEnterDetails", false)
            else Future.successful(())

        } yield (bankDetails, fromEnter) match {
          case (None, _) => Redirect(routes.HasBankAccountController.show)
          case (Some(bankDetails), Some(true)) => Ok(view(bankDetails))
          case _ => Redirect(routes.UkBankAccountDetailsController.show)
        }
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
            bankAccountDetailsService.verifyAndSaveBankAccountDetails(accountDetails, accountType, profile.registrationId).flatMap {
              case BarsSuccess =>
                Future.successful(Redirect(controllers.routes.TaskListController.show.url))
              case BarsLockedOut =>
                bankAccountDetailsService.saveBankAccountNotProvided(DontWantToProvide).map { _ =>
                  Redirect(controllers.errors.routes.BankDetailsLockoutController.show)
                }
              case BarsFailedNotLocked =>
                Future.successful(Redirect(routes.AccountDetailsNotVerifiedController.show))
            }
          case _ => Future.successful(Redirect(routes.HasBankAccountController.show))
        }
      } yield result
    } else {
      Future.successful(Redirect(routes.HasBankAccountController.show))
    }
  }
}
