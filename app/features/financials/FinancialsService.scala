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

import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads }

package services {

  import common.ErrorUtil.fail
  import models._
  import models.api.{VatFinancials, VatScheme}
  import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
  import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
  import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  import scala.concurrent.Future

  trait FinancialsService {
    self: RegistrationService =>

    private val financialsS4LKey: S4LKey[S4LVatFinancials] = S4LVatFinancials.vatFinancials

    def fetchFinancials(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LVatFinancials] = {
      fetchFinancialsFromS4L flatMap {
        case Some(financials) => Future.successful(financials)
        case None => getVatScheme map { vatScheme =>
          vatScheme.financials match {
            case Some(_) => apiToView(vatScheme)
            case None => S4LVatFinancials()
          }
        }
      }
    }

    def submitVatFinancials()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatFinancials] = {
      def merge(fresh: Option[S4LVatFinancials], vs: VatScheme): VatFinancials = fresh.fold(
        vs.financials.getOrElse(throw fail("VatFinancials"))
      )(s4l => S4LVatFinancials.apiT.toApi(s4l))

      for {
        vs <- getVatScheme
        vf <- s4l[S4LVatFinancials]
        response <- vatRegConnector.upsertVatFinancials(profile.registrationId, merge(vf, vs))
      } yield response
    }

    private[services] def apiToView(vs: VatScheme): S4LVatFinancials = S4LVatFinancials(
      estimateVatTurnover       = ApiModelTransformer[EstimateVatTurnover].toViewModel(vs),
      zeroRatedTurnover         = ApiModelTransformer[ZeroRatedSales].toViewModel(vs),
      zeroRatedTurnoverEstimate = ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vs),
      vatChargeExpectancy       = ApiModelTransformer[VatChargeExpectancy].toViewModel(vs),
      vatReturnFrequency        = ApiModelTransformer[VatReturnFrequency].toViewModel(vs),
      accountingPeriod          = ApiModelTransformer[AccountingPeriod].toViewModel(vs),
      companyBankAccount        = ApiModelTransformer[CompanyBankAccount].toViewModel(vs),
      companyBankAccountDetails = ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs)
    )

    private[services] def fetchFinancialsFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LVatFinancials]] = {
      s4LService.fetchAndGetNoAux(financialsS4LKey)
    }
  }
}

package connectors {

  import cats.instances.FutureInstances
  import features.financials.models.{BankAccount, Returns, TurnoverEstimates}
  import models.api.VatFinancials
  import uk.gov.hmrc.http.HttpResponse
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  import scala.concurrent.Future

  trait FinancialsConnector extends FutureInstances {
    self: RegistrationConnector =>

    def getTurnoverEstimates(regId: String)
                            (implicit hc: HeaderCarrier, rds: HttpReads[TurnoverEstimates]): Future[TurnoverEstimates] = {
      http.GET[TurnoverEstimates](s"$vatRegUrl/vatreg/$regId/turnover-estimates") recover {
        case e: Exception => throw logResponse(e, "getTurnoverEstimates")
      }
    }

    def patchTurnoverEstimates(regId: String, turnoverEstimates: TurnoverEstimates)
                              (implicit hc: HeaderCarrier, rds: HttpReads[TurnoverEstimates]): Future[HttpResponse] = {
      http.PATCH[TurnoverEstimates, HttpResponse](s"$vatRegUrl/vatreg/$regId/turnover-estimates", turnoverEstimates) recover {
        case e: Exception => throw logResponse(e, "patchTurnoverEstimates")
      }
    }

    def getReturns(regId: String)
                  (implicit hc: HeaderCarrier, rds: HttpReads[Returns]): Future[Returns] = {
      http.GET[Returns](s"$vatRegUrl/vatreg/$regId/returns") recover {
        case e: Exception => throw logResponse(e, "getReturns")
      }
    }

    def patchReturns(regId: String, returns: Returns)
                    (implicit hc: HeaderCarrier, rds: HttpReads[Returns]): Future[HttpResponse] = {
      http.PATCH[Returns, HttpResponse](s"$vatRegUrl/vatreg/$regId/returns", returns) recover {
        case e: Exception => throw logResponse(e, "patchReturns")
      }
    }

    def getBankAccount(regId: String)
                      (implicit hc: HeaderCarrier, rds: HttpReads[BankAccount]): Future[BankAccount] = {
      http.GET[BankAccount](s"$vatRegUrl/vatreg/$regId/bank-account") recover {
        case e: Exception => throw logResponse(e, "patchBankAccount")
      }
    }

    def patchBankAccount(regId: String, bankAccount: BankAccount)
                        (implicit hc: HeaderCarrier, rds: HttpReads[BankAccount]): Future[HttpResponse] = {
      http.PATCH[BankAccount, HttpResponse](s"$vatRegUrl/vatreg/$regId/bank-account", bankAccount) recover {
        case e: Exception => throw logResponse(e, "patchBankAccount")
      }
    }

    def upsertVatFinancials(regId: String, vatFinancials: VatFinancials)
                           (implicit hc: HeaderCarrier, rds: HttpReads[VatFinancials]): Future[VatFinancials] = {
      http.PATCH[VatFinancials, VatFinancials](s"$vatRegUrl/vatreg/$regId/vat-financials", vatFinancials).recover {
        case e: Exception => throw logResponse(e, "upsertVatFinancials")
      }
    }
  }
}
