/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.{KeystoreConnector, S4LConnector, VatRegistrationConnector}
import controllers.BaseController
import models.IncorpUpdate
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.Future

class DeleteSessionItemsControllerImpl @Inject()(val authConnector: AuthClientConnector,
                                                 val vatRegistrationService: VatRegistrationService,
                                                 val keystoreConnector: KeystoreConnector,
                                                 val currentProfileService: CurrentProfileService,
                                                 val s4LConnector: S4LConnector,
                                                 val messagesApi: MessagesApi,
                                                 val config: ServicesConfig,
                                                 val cancellationService: CancellationService,
                                                 val regConnector : VatRegistrationConnector) extends DeleteSessionItemsController

trait DeleteSessionItemsController extends BaseController with SessionProfile {
  val cancellationService: CancellationService
  val vatRegistrationService: VatRegistrationService
  val regConnector: VatRegistrationConnector
  val currentProfileService: CurrentProfileService
  val s4LConnector: S4LConnector

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
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
      withJsonBody { incorpUpdate =>
        if(incorpUpdate.status == "rejected") {
          for {
            deleteData <- regConnector.clearVatScheme(incorpUpdate.transactionId)
            optRegId <- currentProfileService.addRejectionFlag(incorpUpdate.transactionId)
            clearS4L <- optRegId.map(id => s4LConnector.clear(id)).getOrElse(Future.successful(HttpResponse(200)))
          } yield Ok
        } else {
          Future.successful(Ok)
        }
      }
    }
}
