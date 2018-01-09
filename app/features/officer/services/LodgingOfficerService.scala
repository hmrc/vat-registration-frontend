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

package features.officer.services

import javax.inject.Inject

import common.exceptions.InternalExceptions.NoOfficerFoundException
import config.Logging
import connectors.{RegistrationConnector, S4LConnect}
import features.officer.models.view.LodgingOfficer
import models.CurrentProfile
import models.external.Officer
import features.officer.models.view._
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

  def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    s4lConnector.fetchAndGet[LodgingOfficer](cp.registrationId, LODGING_OFFICER) flatMap {
      case Some(officer) => Future.successful(officer)
      case _ => vatRegistrationConnector.getLodgingOfficer flatMap { json =>
        val lodgingOfficer = json.fold(LodgingOfficer(N, N, N, N, N, N, N))(LodgingOfficer.fromApi)
        s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, lodgingOfficer) map (_ => lodgingOfficer)
      }
    }
  }

  private def getOfficerData(cc: String)(implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[Officer] = {
    incorpInfoService.getOfficerList map { list =>
      list.find(_.name.id == cc).getOrElse(throw new NoOfficerFoundException(cp.registrationId))
    }
  }

  private def updateLodgingOfficer(data: LodgingOfficer)
                                  (implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[LodgingOfficer] = {
    for {
      selectedOfficer <- getOfficerData(data.completionCapacity.getOrElse(throw new NoOfficerFoundException(cp.registrationId)))
      _ <- vatRegistrationConnector.patchLodgingOfficer(data, LodgingOfficer.apiWrites(selectedOfficer))
      _ <- s4lConnector.clear(cp.registrationId)
    } yield data
  }

  private def isModelComplete(lodgingOfficer: LodgingOfficer): Completion[LodgingOfficer] = lodgingOfficer match {
    case LodgingOfficer(Some(_), Some(_), N, N, N, N, N) =>
      Complete(lodgingOfficer)
    case LodgingOfficer(Some(_), Some(_), Some(_), Some(_), Some(fName), fNameDate, Some(_)) if fName.yesNo && fNameDate.isDefined =>
      Complete(lodgingOfficer)
    case LodgingOfficer(Some(_), Some(_), Some(_), Some(_), Some(fName), _, Some(_)) if !fName.yesNo =>
      Complete(lodgingOfficer)
    case _ =>
      Incomplete(lodgingOfficer)
  }

  def updateLodgingOfficer[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    def updateModel(data: T, before: LodgingOfficer): LodgingOfficer = {
      data match {
        case cc: String                         => before.copy(completionCapacity = Some(cc))
        case secu: SecurityQuestionsView => before.copy(securityQuestions = Some(secu))
        case currAddr: HomeAddressView   => before.copy(homeAddress = Some(currAddr))
        case contact: ContactDetailsView => before.copy(contactDetails = Some(contact))
        case fName: FormerNameView              => before.copy(formerName = Some(fName))
        case fNameDate: FormerNameDateView      => before.copy(formerNameDate = Some(fNameDate))
        case prevAddr: PreviousAddressView      => before.copy(previousAddress = Some(prevAddr))
      }
    }

    getLodgingOfficer flatMap { lodgingOfficer =>
      isModelComplete(updateModel(data, lodgingOfficer)).fold(
        incomplete => s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, incomplete) map (_ => incomplete),
        complete => updateLodgingOfficer(complete)
      )
    }
  }

  def updateCompletionCapacity(cc: String)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    getLodgingOfficer flatMap { lodgingOfficer =>
      isModelComplete(lodgingOfficer.copy(completionCapacity = Some(cc))).fold(
        incomplete => s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, incomplete) map (_ => incomplete),
        complete => updateLodgingOfficer(complete)
      )
    }
  }

  def updateSecurityQuestions(secuQuestions: SecurityQuestionsView)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    getLodgingOfficer flatMap { lodgingOfficer =>
      isModelComplete(lodgingOfficer.copy(securityQuestions = Some(secuQuestions))).fold(
        incomplete => s4lConnector.save[LodgingOfficer](cp.registrationId, LODGING_OFFICER, incomplete) map (_ => incomplete),
        complete => updateLodgingOfficer(complete)
      )
    }
  }
}
