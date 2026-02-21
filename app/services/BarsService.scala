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

import com.google.inject.{Inject, Singleton}
import connectors.BarsConnector
import models.BankAccountDetails
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsErrors._
import models.bars._
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class BarsService @Inject()(
                                  barsConnector: BarsConnector
                                )(implicit ec: ExecutionContext) extends Logging {

  def verifyBankDetails(bankAccountType: BankAccountType, bankDetails: BankAccountDetails)(implicit hc: HeaderCarrier): Future[BankAccountDetailsStatus] = {
    val requestBody: JsValue = buildJsonRequestBody(bankAccountType, bankDetails)

    logger.info(s"Verifying bank details for account type: $bankAccountType")

    val callBARS: Future[Either[BarsErrors, BarsVerificationResponse]] = barsConnector.verify(bankAccountType, requestBody)
      .map(checkVerificationResult)
      .recover {
        case _ =>
          logger.error(s"Unexpected error verifying bank details for account type: $bankAccountType")
          Left(DetailsVerificationFailed)
      }

    callBARS.map(handleResponse)
  }

  def checkVerificationResult(successResponse: BarsVerificationResponse
                             ): Either[BarsErrors, BarsVerificationResponse] =
    if (successResponse.isSuccessful) {
      logger.info("BARS verification returned a successful response")
      Right(successResponse)
    }
    else {
      val error = successResponse.check.fold(identity, _ => DetailsVerificationFailed)
      logger.warn(s"BARS verification returned an unsuccessful response: $error")
      Left(error)
    }

  def handleResponse(response: Either[BarsErrors, BarsVerificationResponse]): BankAccountDetailsStatus = response match {
    case Right(_) =>
      logger.info("Bank account details successfully verified")
      ValidStatus
    case Left(BankAccountUnverified) =>
      logger.warn("Bank account details could not be verified — returning indeterminate status")
      IndeterminateStatus
    case Left(error) =>
      logger.error(s"Bank account verification failed due to a service error: $error")
      InvalidStatus
  }

  def buildJsonRequestBody(bankAccountType: BankAccountType, bankDetails: BankAccountDetails): JsValue =
    bankAccountType match {
      case BankAccountType.Personal =>
        Json.toJson(
          BarsPersonalRequest(BarsAccount(bankDetails.sortCode, bankDetails.number), BarsSubject(bankDetails.name))
        )
      case BankAccountType.Business =>
        Json.toJson(
          BarsBusinessRequest(BarsAccount(bankDetails.sortCode, bankDetails.number), BarsBusiness(bankDetails.name))
        )
    }
}