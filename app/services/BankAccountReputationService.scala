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

import connectors.BankAccountReputationConnector
import models.BankAccountDetails
import models.api.BankAccountDetailsStatus
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountReputationService @Inject()(val bankAccountReputationConnector: BankAccountReputationConnector,
                                             val authConnector: AuthConnector,
                                             auditConnector: AuditConnector
                                            )(implicit ec: ExecutionContext) extends AuthorisedFunctions {

  def validateBankDetails(account: BankAccountDetails)(implicit hc: HeaderCarrier): Future[BankAccountDetailsStatus] = {
    bankAccountReputationConnector.validateBankDetails(account).flatMap {
      bankAccountValidationResponse =>
        authorised().retrieve(Retrievals.internalId) {
          case Some(internalId) =>
            val auditEvent = Json.obj(
              "credId" -> internalId,
              "request" -> Json.toJson(account),
              "response" -> bankAccountValidationResponse
            )

            auditConnector.sendExplicitAudit("BarsValidateCheck", auditEvent)

            Future.successful(
              (bankAccountValidationResponse \ "accountNumberWithSortCodeIsValid").as[BankAccountDetailsStatus]
            )
          case None =>
            throw new InternalServerException("Missing internal ID for BARS check auditing")
        }
    }
  }
}



