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

import java.util.UUID
import javax.inject.Inject

import common.enums.IVResult
import common.exceptions.InternalExceptions.ElementNotFoundException
import connectors.{IVConnector, RegistrationConnector}
import features.officer.models.{IVSetup, UserData}
import features.officer.models.view.LodgingOfficer
import features.officer.services.LodgingOfficerService
import models.CurrentProfile
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import services.S4LService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

class IdentityVerificationService @Inject()(config: ServicesConfig,
                                            val ivConnector: IVConnector,
                                            val vatRegistrationConnector: RegistrationConnector,
                                            val vatRegFeatureSwitch: VATRegFeatureSwitches,
                                            val officerService: LodgingOfficerService,
                                            implicit val s4lService: S4LService) extends IVService {
  val ORIGIN             = config.getString("appName")
  val vrfeBaseUrl        = config.getConfString("vat-registration-frontend.www.url", "")
  val vrfeBaseUri        = config.getConfString("vat-registration-frontend.www.uri", "")
}

trait IVService {

  val ivConnector: IVConnector
  val vatRegistrationConnector: RegistrationConnector
  val vatRegFeatureSwitch: VATRegFeatureSwitches
  val s4lService: S4LService
  val officerService: LodgingOfficerService

  val ORIGIN: String
  val vrfeBaseUrl: String
  val vrfeBaseUri: String

  def useIVStub: Boolean = vatRegFeatureSwitch.useIvStub.enabled

  private val CONFIDENCE_LEVEL = 200

  private[services] def buildIVSetupData(lodgingOfficer: LodgingOfficer)(implicit cp: CurrentProfile, hc: HeaderCarrier): IVSetup = {
    val securityQuestions = lodgingOfficer.securityQuestions.getOrElse(throw new ElementNotFoundException(s"No Security questions found for regId: ${cp.registrationId}"))
    val ccView            = lodgingOfficer.completionCapacity.getOrElse(throw new ElementNotFoundException(s"No Completion Capacity View found for regId: ${cp.registrationId}"))
    val officer           = ccView.officer.getOrElse(throw new ElementNotFoundException(s"No Officer found for regId: ${cp.registrationId}"))
    val officerName       = officer.name
    IVSetup(
      origin          = ORIGIN,
      completionURL   = vrfeBaseUrl + features.officer.controllers.routes.IdentityVerificationController.completedIVJourney().url,
      failureURL      = vrfeBaseUrl + vrfeBaseUri + "/ivFailure",
      confidenceLevel = CONFIDENCE_LEVEL,
      userData = UserData(
        firstName   = officerName.forename.getOrElse(throw new ElementNotFoundException(s"First Name not found for regId: ${cp.registrationId}")),
        lastName    = officerName.surname,
        dateOfBirth = securityQuestions.dob.toString,
        nino        = securityQuestions.nino
      )
    )
  }

  def saveJourneyID(json:JsValue)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[CacheMap] = {
    s4lService.saveIv(getJourneyIdFromJson(json))
  }

  def getJourneyIdFromJson(json:JsValue): JsValue = Json.toJson((json \ "journeyLink").toOption.map { a =>
      val s = a.as[String]
      s.substring(s.lastIndexOf("""/""") + 1)
    }.get
  )


  def getIVJourneyID(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[String]] = {
    s4lService.fetchIv
  }

  def setupAndGetIVJourneyURL(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[String] = {
    if(!cp.ivPassed.getOrElse(false)) {
      for {
        officer       <- officerService.getLodgingOfficer
        ivData        =  buildIVSetupData(officer)
        json          <- if (useIVStub) startIVJourney() else ivConnector.setupIVJourney(ivData)
        _             <- saveJourneyID(json)
      } yield (json \ "link").as[String]
    } else {
      Future.successful(features.officer.controllers.routes.OfficerController.showFormerName().url)
    }
  }

  def setIvStatus(ivResult: IVResult.Value)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[Option[IVResult.Value]] = {
    val res = ivResult == IVResult.Success
    vatRegistrationConnector.updateIVStatus(currentProfile.registrationId, res).map(_ => Some(ivResult)).recoverWith {
      case e: Exception =>
        Logger.warn(s"[setIvStatus] Something went wrong for regId: ${currentProfile.registrationId} with message: ${e.getMessage}. returning None")
        Future.successful(None)
    }
  }

  def getJourneyIdAndJourneyOutcome()(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[IVResult.Value] = for {
    id       <- getIVJourneyID
    ivResult <- ivConnector.getJourneyOutcome(id.get)
  } yield ivResult

  private[services] def startIVJourney(journeyid:String = UUID.randomUUID.toString):Future[JsObject] = Future.successful(JsObject(
    Map("link" ->Json.toJson(features.officer.controllers.test.routes.TestIVController.show(journeyid).url), "journeyLink" -> Json.toJson("""/""" + journeyid))
  ))
}
