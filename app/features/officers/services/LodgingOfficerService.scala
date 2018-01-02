/*
 * Copyright 2017 HM Revenue & Customs
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

import common.exceptions.InternalExceptions.{ApiConversionException, NoOfficerFoundException}
import config.Logging
import connectors.{KeystoreConnect, RegistrationConnector, S4LConnect}
import features.officers.models.view.LodgingOfficer
import features.officers.transformers.ToLodgingOfficerView
import models.CurrentProfile
import models.api.{CompletionCapacity, Name}
import models.external.Officer
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerSecurityQuestionsView}
import play.api.libs.json.Reads
import services.IncorporationInfoSrv
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class LodgingOfficerServiceImpl @Inject()(val vatRegistrationConnector: RegistrationConnector,
                                          val incorpInfoService: IncorporationInfoSrv,
                                          val s4lConnector: S4LConnect,
                                          val keystoreConnector: KeystoreConnect) extends LodgingOfficerService

trait LodgingOfficerService extends Logging {
  val incorpInfoService: IncorporationInfoSrv
  val s4lConnector: S4LConnect
  val vatRegistrationConnector: RegistrationConnector
  val keystoreConnector: KeystoreConnect

  type Completion[T] = Either[T, T]
  val Incomplete   = scala.util.Left
  val Complete     = scala.util.Right

  private val LODGING_OFFICER = "LodgingOfficer"
  private val N               = None

  def getCompletionCapacity(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    s4lConnector.fetchAndGet[LodgingOfficer](profile.registrationId, LODGING_OFFICER) flatMap {
      case Some(officer) => Future.successful(officer.comletionCapacity)
      case _             => vatRegistrationConnector.getLodgingOfficer flatMap { json =>
        val lodgingOfficer = json.fold(LodgingOfficer(N,N))(ToLodgingOfficerView.fromApi(_))
               ndde
//        case Some(officer) =>
//          val lodgingOfficer = ToLodgingOfficerView.fromApi(officer)
//          s4lConnector.save[LodgingOfficer](profile.registrationId, LODGING_OFFICER, lodgingOfficer) map {
//            _ => None
//          }
//        case None          => s4lConnector.save[LodgingOfficer](profile.registrationId, LODGING_OFFICER, LodgingOfficer(N,N)) map {
//          _ => None
        }
      }
    }
  }

  def getSecurityDetails(implicit profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[Option[OfficerSecurityQuestionsView]] = {
    s4lConnector.fetchAndGet[LodgingOfficer](profile.registrationId, LODGING_OFFICER) flatMap {
      case Some(officer) =>
      case None => getFromVatRegistration[LodgingOfficer](LodgingOfficer.apiWrites) flatMap {
        case Some(officer) =>
      }
    }
  }

  def submitCompletionCapacity(ccView: CompletionCapacityView)
                              (implicit profile: CurrentProfile, hc: HeaderCarrier, executionContext: ExecutionContext): Future[LodgingOfficer] = {
    for {
      selectedOfficer <- incorpInfoService.getOfficerList map(_.find(_.name.id == ccView.id)
        .getOrElse(throw new NoOfficerFoundException(profile.registrationId)))
      Some(s4lModel)  <- s4lConnector.fetchAndGet[LodgingOfficer](profile.registrationId, LODGING_OFFICER)
      update          =  updateS4LWithCompletionCapacity(s4lModel, selectedOfficer)
      submit          <- determineCompletion(update) match {
        case Right(officer) => for {
          submit <- vatRegistrationConnector.patchLodgingOfficer[LodgingOfficer](officer, LodgingOfficer.apiWrites)
          _      <- s4lConnector.clear(profile.registrationId)
        } yield submit
        case Left(officer)  => s4lConnector.save[LodgingOfficer](profile.registrationId, LODGING_OFFICER, officer) map(_ => officer)
      }
    } yield submit
  }

  private def toSecurityQuestionsView(officer: LodgingOfficer)(implicit currentProfile: CurrentProfile): OfficerSecurityQuestionsView = {
    val dob = officer.dob.getOrElse({
      logger.error(s"[toSecurityQuestionsView] - expected to get dob from api for regId: ${currentProfile.registrationId}")
      throw new ApiConversionException(s"Conversion to OfficerSecurityQuestionsView failed for regId ${currentProfile.registrationId}")
    })

    val nino = officer.nino.getOrElse({
      logger.error(s"[toSecurityQuestionsView] - expected to get nino from api from regId: ${currentProfile.registrationId}")
      throw new ApiConversionException(s"Conversion to OfficerSecurityQuestionsView failed for regId ${currentProfile.registrationId}")
    })
    OfficerSecurityQuestionsView(dob,nino)
  }

  private def determineCompletion(lodgingOfficer: LodgingOfficer): Completion[LodgingOfficer] = {
    lodgingOfficer match {
      case LodgingOfficer(Some(_),_,Some(_),Some(_),Some(_),Some(_)) => Complete(lodgingOfficer)
      case _                                                         => Incomplete(lodgingOfficer)
    }
  }

  private def updateS4LWithCompletionCapacity(lodgingOfficer: LodgingOfficer, officer: Officer): LodgingOfficer = {
    lodgingOfficer.copy(
      first  = officer.name.forename,
      middle = officer.name.otherForenames,
      last   = Some(officer.name.surname),
      role   = Some(officer.role)
    )
  }
}
