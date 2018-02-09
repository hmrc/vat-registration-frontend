/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.internal

import javax.inject.Inject

import common.enums.RegistrationDeletion
import config.AuthClientConnector
import connectors.{KeystoreConnect, RegistrationConnector, S4LConnect}
import controllers.BaseController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{CancellationService, SessionProfile}

class DeleteSessionItemsControllerImpl @Inject()(val authConnector: AuthClientConnector,
                                                 val vatRegistrationConnector: RegistrationConnector,
                                                 val keystoreConnector: KeystoreConnect,
                                                 val save4LaterConnector: S4LConnect,
                                                 val messagesApi: MessagesApi,
                                                 val cancellationService: CancellationService) extends DeleteSessionItemsController

trait DeleteSessionItemsController extends BaseController with SessionProfile {
  val vatRegistrationConnector: RegistrationConnector
  val keystoreConnector: KeystoreConnect
  val save4LaterConnector: S4LConnect
  val cancellationService: CancellationService

  def deleteVatRegistration(regId: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      cancellationService.deleteVatRegistration(regId) map {
        case RegistrationDeletion.deleted   => Ok
        case RegistrationDeletion.forbidden =>
          logger.warn(s"[deleteVatRegistration] - Requested document regId $regId to be deleted is not corresponding to the CurrentProfile regId")
          BadRequest
      } recover {
        case ex =>
          logger.error(s"[RegistrationController] [delete] - Received an error when deleting Registration regId: $regId - error: ${ex.getMessage}")
          InternalServerError
      }
  }
}
