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
import models.api._
import models.bars._
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class BarsService @Inject() (barsConnector: BarsConnector)(implicit ec: ExecutionContext) extends Logging {

  def verifyBankDetails(bankDetails: BankAccountDetails, bankAccountType: BankAccountType)(implicit
      hc: HeaderCarrier): Future[BarsResponseAndVerificationStatus] = {

    val requestBody: JsValue = buildJsonRequestBody(bankDetails, bankAccountType)

    barsConnector
      .verify(bankAccountType, requestBody)
      .map { response =>
        val (bankAccountDetailsStatus, listOfBarsErrorsAndReasons) = response.handleVerificationResponse
        val optErrorReasons =
          if (listOfBarsErrorsAndReasons.isEmpty) "" else listOfBarsErrorsAndReasons.map(_.barsError).mkString(" - Failure reasons: ", ", ", ".")
        logger.info(s"Verification result for ${bankAccountType.asBars} bank details: $bankAccountDetailsStatus$optErrorReasons")
        BarsResponseAndVerificationStatus(bankAccountDetailsStatus, barsVerificationResponse = Some(response))
      }
      .recover { case e =>
        logger.error(s"Unexpected error when verifying ${bankAccountType.asBars} bank details: ${e.getMessage}")
        BarsResponseAndVerificationStatus(InvalidStatus, barsVerificationResponse = None)
      }
  }

  def buildJsonRequestBody(bankDetails: BankAccountDetails, bankAccountType: BankAccountType): JsValue = {
    val barsAccount = BarsAccount(bankDetails.sortCode, bankDetails.number, bankDetails.rollNumber)
    bankAccountType match {
      case BankAccountType.Personal =>
        Json.toJson(BarsPersonalRequest(barsAccount, BarsSubject(bankDetails.name)))
      case BankAccountType.Business =>
        Json.toJson(BarsBusinessRequest(barsAccount, BarsBusiness(bankDetails.name)))
    }
  }

}
