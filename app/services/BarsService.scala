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

import connectors.BarsConnector
import models.api.bars.request._
import models.api.bars.response.VerifyResponse._
import models.api.bars.response._
import models.api.bars.{BarsTypeOfBankAccount, BarsTypesOfBankAccount}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.HttpResponseUtils._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsService @Inject()(barsConnector: BarsConnector)(implicit ec: ExecutionContext)
  extends Logging {

  def verifyPersonal(
                      bankAccount: BarsBankAccount,
                      subject: BarsSubject
                    )(implicit hc: HeaderCarrier): Future[BarsResponse] =
    barsConnector
      .verifyPersonal(BarsVerifyPersonalRequest(bankAccount, subject))
      .map(parseVerifyResponse)

  def verifyBusiness(
                      bankAccount: BarsBankAccount,
                      business: BarsBusiness
                    )(implicit hc: HeaderCarrier): Future[BarsResponse] =
    barsConnector
      .verifyBusiness(BarsVerifyBusinessRequest(bankAccount, business))
      .map(parseVerifyResponse)


  def verifyBankDetails(
                         bankAccount:       BarsBankAccount,
                         subject:           BarsSubject,
                         business:          BarsBusiness,
                         typeOfBankAccount: BarsTypeOfBankAccount
                       )(implicit hc: HeaderCarrier): Future[Either[BarsError, VerifyResponse]] = {

    val verifyCall: Future[BarsResponse] =
      typeOfBankAccount match {
        case BarsTypesOfBankAccount.Personal => verifyPersonal(bankAccount, subject)
        case BarsTypesOfBankAccount.Business => verifyBusiness(bankAccount, business)
      }

    verifyCall.map {
      case v: VerifyResponse        => handleVerifyResponse(v)
      case s: SortCodeOnDenyList    => Left(SortCodeOnDenyListErrorResponse(s))
    }
  }


  private def parseVerifyResponse(httpResponse: HttpResponse): BarsResponse =
    httpResponse.status match {
      case OK =>
        httpResponse
          .parseJSON[BarsVerifyResponse]
          .map(VerifyResponse.apply)
          .getOrElse(throw UpstreamErrorResponse(httpResponse.body, httpResponse.status))

      case BAD_REQUEST =>
        httpResponse.json.validate[BarsErrorResponse] match {
          case JsSuccess(err, _) if err.code == "SORT_CODE_ON_DENY_LIST" =>
            SortCodeOnDenyList(err)

          case _ =>
            throw UpstreamErrorResponse(httpResponse.body, httpResponse.status)
        }

      case _ =>
        throw UpstreamErrorResponse(httpResponse.body, httpResponse.status)
    }


  private def handleVerifyResponse(response: VerifyResponse): Either[BarsError, VerifyResponse] = {

    response match {
      case verifySuccess()                  => Right(response)
      case thirdPartyError()                => Left(ThirdPartyError(response))
      case accountNumberIsWellFormattedNo() => Left(AccountNumberNotWellFormatted(response))
      case sortCodeIsPresentOnEiscdNo()     => Left(SortCodeNotPresentOnEiscd(response))
      case sortCodeSupportsDirectDebitNo()  => Left(SortCodeDoesNotSupportDirectDebit(response))
      case nameMatchesNo()                  => Left(NameDoesNotMatch(response))
      case accountDoesNotExist()            => Left(AccountDoesNotExist(response))
      case _                                => Left(OtherBarsError(response))
    }
  }
}