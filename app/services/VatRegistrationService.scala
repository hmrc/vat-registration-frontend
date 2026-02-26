/*
 * Copyright 2026 HM Revenue & Customs
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
import models.error.MissingAnswerException
import play.api.i18n.MessagesApi
import play.api.libs.json.{Format, Json, Reads}
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}

@Singleton
class VatRegistrationService @Inject()(vatRegConnector: VatRegistrationConnector,
                                       registrationApiConnector: RegistrationApiConnector,
                                       val sessionService: SessionService,
                                       val auditConnector: AuditConnector,
                                       val authConnector: AuthConnector
                                      ) (implicit ec: ExecutionContext) extends AuthorisedFunctions {

  val missingRegReasonSection = "tasklist.eligibilty.regReason"

  def getVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[VatScheme] =
    vatRegConnector.getRegistration[VatScheme](profile.registrationId)

  def upsertVatScheme(vatScheme: VatScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[VatScheme] =
    vatRegConnector.upsertRegistration(profile.registrationId, vatScheme)

  def getAllRegistrations(implicit hc: HeaderCarrier, request: Request[_]): Future[List[VatSchemeHeader]] =
    vatRegConnector.getAllRegistrations.map(_.filter(_.createdDate.isAfter(LocalDate.MIN.plusDays(1)))) //Sanity check to guard against broken schemes

  def getSection[T](regId: String)(implicit hc: HeaderCarrier, format: Format[T], apiKey: ApiKey[T], request: Request[_]): Future[Option[T]] =
    registrationApiConnector.getSection[T](regId)

  def upsertSection[T](regId: String, data: T)(implicit hc: HeaderCarrier, format: Format[T], apiKey: ApiKey[T], request: Request[_]): Future[T] =
    registrationApiConnector.replaceSection[T](regId, data)

  def getVatSchemeHeader(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[VatSchemeHeader] = {
    implicit val reads: Reads[VatSchemeHeader] = VatSchemeHeader.vatSchemeReads
    vatRegConnector.getRegistration[VatSchemeHeader](regId)
  }

  def getAckRef(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    implicit val key: ApiKey[String] = acknowledgementReferenceKey

    getSection[String](regId).map(_.getOrElse(throw new InternalServerException("Missing Acknowledgement Reference")))
  }

  def createRegistrationFootprint(implicit hc: HeaderCarrier, request: Request[_]): Future[VatScheme] = {
    logger.info("[createRegistrationFootprint] Creating registration footprint")
    vatRegConnector.createNewRegistration
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[VatRegStatus.Value] = {
    getSection[VatRegStatus.Value](regId).map(_.getOrElse(throw new InternalServerException("Missing Vat Registration Status")))
  }

  def submitRegistration()(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_], messagesApi: MessagesApi): Future[DESResponse] = {
    val lang = request.cookies.get(messagesApi.langCookieName) match {
      case Some(langCookie) => langCookie.value
      case _ => "en"
    }

    vatRegConnector.submitRegistration(profile.registrationId, request.headers.toSimpleMap, lang)
  }

  def getEligibilitySubmissionData(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[EligibilitySubmissionData] =
    registrationApiConnector.getSection[EligibilitySubmissionData](profile.registrationId).map(optData =>
      optData.getOrElse {
        errorLog("[VatRegistrationService][getEligibilitySubmissionData] missiing registration reason section")
        throw MissingAnswerException(missingRegReasonSection)
      }
    )

  def partyType(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[PartyType] =
    getEligibilitySubmissionData.map(_.partyType)

  def isTransactor(implicit profile: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[Boolean] =
    getEligibilitySubmissionData.map(_.isTransactor)

  def raiseAuditEvent(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Unit] = {
    logger.info("Raising an explicit audit event for StartRegistration!")
    authorised().retrieve(Retrievals.credentials) {
      case Some(credential) =>
        val auditEventDetail = Json.obj(
          "authProviderId" -> credential.providerId,
          "journeyId" -> profile.registrationId
        )
        Future.successful(auditConnector.sendExplicitAudit("StartRegistration", auditEventDetail))
      case _ =>
        throw new InternalServerException("Missing credentials for startRegistration auditing")
    }
  }
}