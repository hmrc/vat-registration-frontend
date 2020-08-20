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

import java.util.UUID

import common.exceptions.InternalExceptions.ElementNotFoundException
import connectors.{IVConnector, KeystoreConnector, VatRegistrationConnector}
import javax.inject.{Inject, Singleton}
import models.external.Name
import models.view.LodgingOfficer
import models.{CurrentProfile, IVResult, IVSetup, UserData}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

@Singleton
class IVService @Inject()(config: ServicesConfig,
                          val ivConnector: IVConnector,
                          val vatRegistrationConnector: VatRegistrationConnector,
                          val vatRegFeatureSwitch: VATRegFeatureSwitches,
                          val keystoreConnector: KeystoreConnector,
                          val s4lService: S4LService) {
  val ORIGIN: String = config.getString("appName")
  val vrfeBaseUrl: String = config.getConfString("vat-registration-frontend.www.url", "")
  val vrfeBaseUri: String = config.getConfString("vat-registration-frontend.www.uri", "")

  def useIVStub: Boolean = vatRegFeatureSwitch.useIvStub.enabled

  private val CONFIDENCE_LEVEL = 200

  private[services] def buildIVSetupData(lodgingOfficer: LodgingOfficer, applicant: Name, nino: String)(implicit cp: CurrentProfile, hc: HeaderCarrier): IVSetup = {
    val securityQuestions = lodgingOfficer.securityQuestions.getOrElse(throw new ElementNotFoundException(s"No Security questions found for regId: ${cp.registrationId}"))
    IVSetup(
      origin = ORIGIN,
      completionURL = vrfeBaseUrl + controllers.routes.IdentityVerificationController.completedIVJourney().url,
      failureURL = vrfeBaseUrl + vrfeBaseUri + "/ivFailure",
      confidenceLevel = CONFIDENCE_LEVEL,
      userData = UserData(
        firstName = applicant.forename.getOrElse(throw new ElementNotFoundException(s"First Name not found for regId: ${cp.registrationId}")),
        lastName = applicant.surname,
        dateOfBirth = securityQuestions.dob.toString,
        nino = nino
      )
    )
  }

  def getJourneyIdFromJson(json: JsValue): String = (json \ "journeyLink").validateOpt[String].get.map(s => s.substring(s.lastIndexOf("/") + 1)).getOrElse {
    throw new IllegalStateException(s"[IVService][getJourneyIdFromJson] an error occurred, missing the journeyLink in json: $json")
  }

  def setupAndGetIVJourneyURL(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[String] = {
    for {
      oOfficer <- vatRegistrationConnector.getLodgingOfficer(cp.registrationId)
      jsonOfficer = oOfficer.getOrElse(throw new IllegalStateException(s"No officer found for regId: ${cp.registrationId}"))
      ivData = buildIVSetupData(LodgingOfficer.fromApi(jsonOfficer), LodgingOfficer.fromJsonToName(jsonOfficer), (jsonOfficer \ "nino").as[String])
      json <- if (useIVStub) Future.successful(startIVStubJourney()) else ivConnector.setupIVJourney(ivData)
      _ <- s4lService.save("IVJourneyID", getJourneyIdFromJson(json))
    } yield (json \ "link").as[String]
  }


  private def setIVStatus(ivResult: IVResult.Value)(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[IVResult.Value] = {
    val ivPassed = ivResult == IVResult.Success
    for {
      _ <- vatRegistrationConnector.updateIVStatus(currentProfile.registrationId, ivPassed)
    } yield ivResult
  }

  private[services] def startIVStubJourney(journeyid: String = UUID.randomUUID.toString): JsObject = Json.obj(
    "link" -> controllers.test.routes.TestIVController.show(journeyid).url,
    "journeyLink" -> s"/$journeyid"
  )

  def getIVStatus(regId: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    vatRegistrationConnector.getLodgingOfficer(regId) map (_.flatMap(LodgingOfficer.ivStatusfromApi))
  }

  def fetchAndSaveIVStatus(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[IVResult.Value] = {
    for {
      id <- s4lService.fetchAndGet[String]("IVJourneyID")
      ivResult <- ivConnector.getJourneyOutcome(id.get)
      _ <- setIVStatus(ivResult)
    } yield ivResult
  }
}
