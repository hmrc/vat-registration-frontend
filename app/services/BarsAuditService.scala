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

import models.BankAccountDetails
import models.bars.{BankAccountType, BarsVerificationResponse}
import models.CurrentProfile
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsAuditService @Inject() (
                                   auditConnector:    AuditConnector,
                                   val authConnector: AuthConnector
                                 ) extends Logging
  with AuthorisedFunctions {

  private val BarsCheckAttemptAuditType = "BarsCheckAttempt"

  def sendBarsAuditEvent(
                          bankAccountDetails: BankAccountDetails,
                          bankAccountType:    BankAccountType,
                          rawResponse:        Option[BarsVerificationResponse],
                          attemptNumber:      Int,
                          accountStatus:      String,
                          checkOutcome:       String
                        )(implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[Unit] =
    retrieveIdentityDetails().map { case (credId, userType) =>
      logger.info(s"Raising BARS audit event: outcome=$checkOutcome attempt=$attemptNumber accountStatus=$accountStatus")

      val detail: JsObject = Json.obj(
        "attemptNumber" -> attemptNumber,
        "accountStatus" -> accountStatus,
        "checkOutcome"  -> checkOutcome,
        "credId"        -> credId,
        "journeyId"     -> profile.registrationId,
        "userType"      -> userType,
        "detailsSubmitted" -> Json.obj(
          "account" -> Json.obj(
            "sortCode"      -> bankAccountDetails.sortCode,
            "accountNumber" -> bankAccountDetails.number
          ),
          "accountType" -> bankAccountType.toString.toLowerCase,
          "accountName" -> bankAccountDetails.name
        ),
        "validationResponse" -> rawResponse.map { r =>
          Json.obj(
            "accountExists" -> r.accountExists.toString.toLowerCase,
            "nameMatches"   -> r.nameMatches.toString.toLowerCase
          )
        }
      )

      auditConnector.sendExplicitAudit(BarsCheckAttemptAuditType, detail)
    }

  private def retrieveIdentityDetails()(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[(String, String)] =
    for {
      credId <- authorised().retrieve(Retrievals.internalId) {
        case Some(id) => Future.successful(id)
        case None     => Future.failed(new InternalServerException("Missing internal ID for BARS check auditing"))
      }
      affinity <- authorised().retrieve(Retrievals.affinityGroup) {
        case Some(group) => Future.successful(group.toString.toLowerCase)
        case None        => Future.failed(new InternalServerException("Missing affinity group for BARS check auditing"))
      }
    } yield (credId, affinity)
}