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
import play.api.mvc.{Action, AnyContent}
import services.{LockService, SessionService}
import views.html.bankdetails.AccountDetailsNotVerifiedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AccountDetailsNotVerifiedController @Inject() (
    val authConnector: AuthClientConnector,
    val sessionService: SessionService,
    lockService: LockService,
    view: AccountDetailsNotVerifiedView
)(implicit appConfig: FrontendAppConfig, val executionContext: ExecutionContext, baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    lockService.redirectIfBarsIsLocked {
      lockService.getBarsAttemptsUsed(profile.registrationId).map { attemptsUsed =>
        Ok(view(attemptsUsed))
      }
    }
  }

}
