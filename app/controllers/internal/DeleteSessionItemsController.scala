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
import connectors.{KeystoreConnect, S4LConnect, VatRegistrationConnector}
import controllers.BaseController
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsPath, JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent}
import services.{CancellationService, SessionProfile, VatRegistrationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeleteSessionItemsControllerImpl @Inject()(val authConnector: AuthClientConnector,
                                                 val vatRegistrationService: VatRegistrationService,
                                                 val keystoreConnector: KeystoreConnect,
                                                 val save4LaterConnector: S4LConnect,
                                                 val messagesApi: MessagesApi,
                                                 val cancellationService: CancellationService,
                                                 val regConnector : VatRegistrationConnector) extends DeleteSessionItemsController

trait DeleteSessionItemsController extends BaseController with SessionProfile {
  val cancellationService: CancellationService
  val vatRegistrationService: VatRegistrationService
  val regConnector : VatRegistrationConnector

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

  def deleteIfRejected(): Action[JsValue] = Action.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody { incorpUpdate =>
        implicit val hc: HeaderCarrier = HeaderCarrier()
        if(incorpUpdate.transaction_status == "rejected") {
          regConnector.clearVatScheme(incorpUpdate.`_id`) map { _ => Ok }
        } else {
          Future.successful(Ok)
        }
      }
    }
}

case class IncorpUpdate(`_id`: String, transaction_status: String)

object IncorpUpdate {
  implicit val format: OFormat[IncorpUpdate] = Json.format[IncorpUpdate]
}