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

package services

import config.Logging
import connectors.RegistrationConnector
import javax.inject.Inject
import models.external.Name
import models.view.{LodgingOfficer, _}
import models.{CurrentProfile, S4LKey}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class LodgingOfficerServiceImpl @Inject()(val vatRegistrationConnector: RegistrationConnector,
                                          val s4LService: S4LService) extends LodgingOfficerService

trait LodgingOfficerService extends Logging {
  val vatRegistrationConnector: RegistrationConnector
  val s4LService: S4LService

  private val N = None

  def getApplicantName(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Name] = {
    vatRegistrationConnector.getLodgingOfficer(cp.registrationId) map { res =>
      res.fold(throw new IllegalStateException(s"[LodgingOfficerService] [getApplicantName] Can't determine applicant Name for regId: ${cp.registrationId}"))(LodgingOfficer.fromJsonToName)
    }
  }

  def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    s4LService.fetchAndGetNoAux[LodgingOfficer](S4LKey[LodgingOfficer]) flatMap {
      case Some(officer) => Future.successful(officer)
      case _ => vatRegistrationConnector.getLodgingOfficer(cp.registrationId) flatMap { json =>
        val lodgingOfficer = json.fold(LodgingOfficer(N, N, N, N, N, N))(LodgingOfficer.fromApi)
        s4LService.saveNoAux[LodgingOfficer](lodgingOfficer, S4LKey[LodgingOfficer]) map (_ => lodgingOfficer)
      }
    }
  }

  private def updateLodgingOfficer(data: LodgingOfficer)
                                  (implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[LodgingOfficer] = {
    for {
      _ <- vatRegistrationConnector.patchLodgingOfficer(data)
      _ <- s4LService.clear
    } yield data
  }

  private def isModelComplete(lodgingOfficer: LodgingOfficer): Completion[LodgingOfficer] = lodgingOfficer match {
    case LodgingOfficer(Some(_), N, N, N, N, N) =>
      Complete(lodgingOfficer)
    case LodgingOfficer(Some(_), Some(_), Some(_), Some(fName), fNameDate, Some(_)) if fName.yesNo && fNameDate.isDefined =>
      Complete(lodgingOfficer)
    case LodgingOfficer(Some(_), Some(_), Some(_), Some(fName), _, Some(_)) if !fName.yesNo =>
      Complete(lodgingOfficer)
    case _ =>
      Incomplete(lodgingOfficer)
  }

  def saveLodgingOfficer[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
    def updateModel(data: T, before: LodgingOfficer): LodgingOfficer = {
      data match {
        case secu: SecurityQuestionsView => before.copy(securityQuestions = Some(secu))
        case currAddr: HomeAddressView => before.copy(homeAddress = Some(currAddr))
        case contact: ContactDetailsView => before.copy(contactDetails = Some(contact))
        case fName: FormerNameView => before.copy(formerName = Some(fName))
        case fNameDate: FormerNameDateView => before.copy(formerNameDate = Some(fNameDate))
        case prevAddr: PreviousAddressView => before.copy(previousAddress = Some(prevAddr))
      }
    }

    getLodgingOfficer flatMap { lodgingOfficer =>
      isModelComplete(updateModel(data, lodgingOfficer)).fold(
        incomplete => s4LService.saveNoAux[LodgingOfficer](incomplete, S4LKey[LodgingOfficer]) map (_ => incomplete),
        complete => updateLodgingOfficer(complete)
      )
    }
  }
}
