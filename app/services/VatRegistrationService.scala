/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.RegistrationApiConnector.acknowledgementReferenceKey
import connectors._
import models._
import models.api._
import play.api.libs.json.{Format, JsValue}
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val s4LService: S4LService,
                                       vatRegConnector: VatRegistrationConnector,
                                       registrationApiConnector: RegistrationApiConnector,
                                       val sessionService: SessionService
                                      )(implicit ec: ExecutionContext) {

  // -- New Registrations API methods --

  def getVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.getRegistration(profile.registrationId)

  def upsertVatScheme(vatScheme: VatScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.upsertRegistration(profile.registrationId, vatScheme)

  def getAllRegistrations(implicit hc: HeaderCarrier): Future[List[VatSchemeHeader]] =
    vatRegConnector.getAllRegistrations

  def getSection[T](regId: String)(implicit hc: HeaderCarrier, format: Format[T], apiKey: ApiKey[T]): Future[Option[T]] =
    registrationApiConnector.getSection[T](regId)

  def upsertSection[T](regId: String, data: T)(implicit hc: HeaderCarrier, format: Format[T], apiKey: ApiKey[T]): Future[T] =
    registrationApiConnector.replaceSection[T](regId, data)

  // -- End new Registrations API methods --

  def getVatSchemeJson(regId: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    vatRegConnector.getRegistrationJson(regId)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    implicit val key: ApiKey[String] = acknowledgementReferenceKey

    getSection[String](regId).map(_.getOrElse(throw new InternalServerException("Missing Acknowledgement Reference")))
  }

  def createRegistrationFootprint(implicit hc: HeaderCarrier): Future[VatScheme] = {
    logger.info("[createRegistrationFootprint] Creating registration footprint")
    vatRegConnector.createNewRegistration
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = {
    getSection[VatRegStatus.Value](regId).map(_.getOrElse(throw new InternalServerException("Missing Vat Registration Status")))
  }

  def submitRegistration()(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[DESResponse] = {
    vatRegConnector.submitRegistration(profile.registrationId, request.headers.toSimpleMap)
  }

  def getEligibilitySubmissionData(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[EligibilitySubmissionData] =
    registrationApiConnector.getSection[EligibilitySubmissionData](profile.registrationId).map(optData =>
      optData.getOrElse(throw new IllegalStateException(s"No EligibilitySubmissionData block found in the backend for regId: ${profile.registrationId}"))
    )

  def partyType(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[PartyType] =
    getEligibilitySubmissionData.map(_.partyType)

  def isTransactor(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] =
    getEligibilitySubmissionData.map(_.isTransactor)
}