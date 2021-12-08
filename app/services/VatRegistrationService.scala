/*
 * Copyright 2021 HM Revenue & Customs
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

import common.enums.VatRegStatus
import connectors._
import models.api._
import models.{TurnoverEstimates, _}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val s4LService: S4LService,
                                       vatRegConnector: VatRegistrationConnector,
                                       registrationApiConnector: RegistrationApiConnector,
                                       val keystoreConnector: KeystoreConnector
                                      )(implicit ec: ExecutionContext) {

  def getVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.getRegistration(profile.registrationId)

  def getAllRegistrations(implicit hc: HeaderCarrier): Future[List[VatSchemeHeader]] =
    vatRegConnector.getAllRegistrations

  def getVatSchemeJson(regId: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    vatRegConnector.getRegistrationJson(regId)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): Future[String] = vatRegConnector.getAckRef(regId)

  def getTaxableThreshold(date: LocalDate = LocalDate.now())(implicit hc: HeaderCarrier): Future[String] = {
    vatRegConnector.getTaxableThreshold(date) map { taxableThreshold =>
      "%,d".format(taxableThreshold.threshold.toInt)
    }
  }

  def deleteVatScheme(registrationId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    vatRegConnector.deleteVatScheme(registrationId)

  def createRegistrationFootprint(implicit hc: HeaderCarrier): Future[VatScheme] = {
    logger.info("[createRegistrationFootprint] Creating registration footprint")
    vatRegConnector.createNewRegistration
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = vatRegConnector.getStatus(regId)

  def getEligibilityData(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[JsObject] = vatRegConnector.getEligibilityData

  def submitRegistration()(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[DESResponse] = {
    vatRegConnector.submitRegistration(profile.registrationId, request.headers.toSimpleMap)
  } recover {
    case _ => SubmissionFailedRetryable
  }

  def getThreshold(regId: String)(implicit hc: HeaderCarrier): Future[Threshold] =
    vatRegConnector.getThreshold(regId) map (_.getOrElse(throw new IllegalStateException(s"No threshold block found in the back end for regId: $regId")))

  def fetchTurnoverEstimates(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[TurnoverEstimates]] = {
    vatRegConnector.getTurnoverEstimates
  }

  def submitHonestyDeclaration(regId: String, honestyDeclaration: Boolean)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    vatRegConnector.submitHonestyDeclaration(regId, honestyDeclaration)
  }

  def storePartialVatScheme(regId: String, partialVatScheme: JsValue)(implicit hc: HeaderCarrier): Future[JsValue] =
    vatRegConnector.upsertVatScheme(regId, partialVatScheme)

  def getEligibilitySubmissionData(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[EligibilitySubmissionData] =
    registrationApiConnector.getSection[EligibilitySubmissionData](profile.registrationId).map(optData =>
      optData.getOrElse(throw new IllegalStateException(s"No EligibilitySubmissionData block found in the backend for regId: ${profile.registrationId}"))
    )

  def partyType(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[PartyType] =
    getEligibilitySubmissionData.map(_.partyType)

  def isTransactor(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] =
    getEligibilitySubmissionData.map(_.isTransactor)
}