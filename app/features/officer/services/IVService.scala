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
import connectors.{IVConnector, KeystoreConnector, RegistrationConnector}
import features.officer.models.view.LodgingOfficer
import features.officer.models.{IVSetup, UserData}
import models.CurrentProfile
import play.api.libs.json.{JsObject, JsValue, Json}
import services.S4LService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

class IVServiceImpl @Inject()(config: ServicesConfig,
                              val ivConnector: IVConnector,
                              val vatRegistrationConnector: RegistrationConnector,
                              val vatRegFeatureSwitch: VATRegFeatureSwitches,
                              val officerService: LodgingOfficerService,
                              val keystoreConnector: KeystoreConnector,
                              val s4lService: S4LService) extends IVService {
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
  val keystoreConnector: KeystoreConnector

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

  def getJourneyIdFromJson(json: JsValue): String = (json \ "journeyLink").validateOpt[String].get.map(s => s.substring(s.lastIndexOf("/") + 1)).getOrElse {
    throw new IllegalStateException(s"[IVService][getJourneyIdFromJson] an error occurred, missing the journeyLink in json: $json")
  }

  def setupAndGetIVJourneyURL(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[String] = {
    if(!cp.ivPassed.contains(true)) {
      for {
        officer       <- officerService.getLodgingOfficer
        ivData        =  buildIVSetupData(officer)
        json          <- if (useIVStub) Future.successful(startIVStubJourney()) else ivConnector.setupIVJourney(ivData)
        _             <- s4lService.save("IVJourneyID", getJourneyIdFromJson(json))
      } yield (json \ "link").as[String]
    } else {
      Future.successful(features.officer.controllers.routes.OfficerController.showFormerName().url)
    }
  }

  private def setIVStatus(ivResult: IVResult.Value)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[IVResult.Value] = {
    val ivPassed = ivResult == IVResult.Success
    for {
      _ <- vatRegistrationConnector.updateIVStatus(currentProfile.registrationId, ivPassed)
      _ <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(ivPassed = Some(ivPassed)))
    } yield ivResult
  }

  private[services] def startIVStubJourney(journeyid: String = UUID.randomUUID.toString): JsObject = Json.obj(
    "link" -> features.officer.controllers.test.routes.TestIVController.show(journeyid).url,
    "journeyLink" -> s"/$journeyid"
  )

  def getIVStatus(regId: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    vatRegistrationConnector.getLodgingOfficer(regId) map (_.flatMap(LodgingOfficer.ivStatusfromApi))
  }

  def fetchAndSaveIVStatus(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[IVResult.Value] = {
    for {
      id       <- s4lService.fetchAndGet[String]("IVJourneyID")
      ivResult <- ivConnector.getJourneyOutcome(id.get)
      _        <- setIVStatus(ivResult)
    } yield ivResult
  }
}
