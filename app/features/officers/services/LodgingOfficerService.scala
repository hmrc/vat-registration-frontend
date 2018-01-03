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

package features.officers.services

import javax.inject.Inject

import common.exceptions.InternalExceptions.NoOfficerFoundException
import config.Logging
import connectors.{RegistrationConnector, S4LConnect}
import features.officers.models.view.LodgingOfficer
import features.officers.transformers.ToLodgingOfficerView
import models.CurrentProfile
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import services.IncorporationInfoSrv
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class LodgingOfficerServiceImpl @Inject()(val vatRegistrationConnector: RegistrationConnector,
                                          val incorpInfoService: IncorporationInfoSrv,
                                          val s4lConnector: S4LConnect) extends LodgingOfficerService

trait LodgingOfficerService extends Logging {
  val incorpInfoService: IncorporationInfoSrv
  val s4lConnector: S4LConnect
  val vatRegistrationConnector: RegistrationConnector

  type Completion[T] = Either[T, T]
  val Incomplete   = scala.util.Left
  val Complete     = scala.util.Right

  private val LODGING_OFFICER = "LodgingOfficer"
  private val N               = None

  def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] =
    s4lConnector.fetchAndGet[LodgingOfficer](cp.registrationId, LODGING_OFFICER) flatMap {
      case Some(officer) => Future.successful(officer)
      case _             => vatRegistrationConnector.getLodgingOfficer flatMap { json =>
        val lodgingOfficer = json.fold(LodgingOfficer(N,N))(ToLodgingOfficerView.fromApi)
        s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, lodgingOfficer) map (_ => lodgingOfficer)
      }
    }

  private def getOfficerData(cc: String)(implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[Officer] =
    incorpInfoService.getOfficerList map { list =>
      list.find(_.name.id == cc).getOrElse(throw new NoOfficerFoundException(cp.registrationId))
    }

  private def updateLodgingOfficer(data: LodgingOfficer)
                                  (implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[LodgingOfficer] =
    for {
      selectedOfficer <- getOfficerData(data.completionCapacity.getOrElse(throw new NoOfficerFoundException(cp.registrationId)))
      _ <- vatRegistrationConnector.patchLodgingOfficer(data, LodgingOfficer.apiWrites(selectedOfficer))
      _ <- s4lConnector.clear(cp.registrationId)
    } yield data

  private def isModelComplete(lodgingOfficer: LodgingOfficer): Completion[LodgingOfficer] = lodgingOfficer match {
    case LodgingOfficer(Some(_), Some(_)) => Complete(lodgingOfficer)
    case _ => Incomplete(lodgingOfficer)
  }

  def updateCompletionCapacity(cc: String)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] =
    getLodgingOfficer flatMap { lodgingOfficer =>
      isModelComplete(lodgingOfficer.copy(completionCapacity = Some(cc))).fold(
        incomplete => s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, incomplete) map (_ => incomplete),
        complete => updateLodgingOfficer(complete)
      )
    }
}
