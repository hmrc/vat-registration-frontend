/*
 * Copyright 2017 HM Revenue & Customs
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

package models.api {
  import play.api.libs.json._

  case class VatFinancials(bankAccount: Option[VatBankAccount] = None,
                           turnoverEstimate: Long,
                           zeroRatedTurnoverEstimate: Option[Long] = None,
                           reclaimVatOnMostReturns: Boolean,
                           accountingPeriods: VatAccountingPeriod)

  object VatFinancials {
    implicit val format: OFormat[VatFinancials] = Json.format[VatFinancials]
  }
}

package models {

  import common.ErrorUtil.fail
  import models.api.{VatAccountingPeriod, VatBankAccount, VatFinancials, VatScheme}
  import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
  import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
  import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.MONTHLY
  import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
  import play.api.libs.json.{Json, OFormat}

  final case class S4LVatFinancials(estimateVatTurnover: Option[EstimateVatTurnover] = None,
                                    zeroRatedTurnover: Option[ZeroRatedSales] = None,
                                    zeroRatedTurnoverEstimate: Option[EstimateZeroRatedSales] = None,
                                    vatChargeExpectancy: Option[VatChargeExpectancy] = None,
                                    vatReturnFrequency: Option[VatReturnFrequency] = None,
                                    accountingPeriod: Option[AccountingPeriod] = None,
                                    companyBankAccount: Option[CompanyBankAccount] = None,
                                    companyBankAccountDetails: Option[CompanyBankAccountDetails] = None)

  object S4LVatFinancials {
    implicit val format: OFormat[S4LVatFinancials] = Json.format[S4LVatFinancials]
    implicit val vatFinancials: S4LKey[S4LVatFinancials] = S4LKey("VatFinancials")

    implicit val modelT = new S4LModelTransformer[S4LVatFinancials] {
      // map VatScheme to S4LVatFinancials
      override def toS4LModel(vs: VatScheme): S4LVatFinancials =
        S4LVatFinancials(
          estimateVatTurnover = ApiModelTransformer[EstimateVatTurnover].toViewModel(vs),
          zeroRatedTurnover = ApiModelTransformer[ZeroRatedSales].toViewModel(vs),
          zeroRatedTurnoverEstimate = ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vs),
          vatChargeExpectancy = ApiModelTransformer[VatChargeExpectancy].toViewModel(vs),
          vatReturnFrequency = ApiModelTransformer[VatReturnFrequency].toViewModel(vs),
          accountingPeriod = ApiModelTransformer[AccountingPeriod].toViewModel(vs),
          companyBankAccount = ApiModelTransformer[CompanyBankAccount].toViewModel(vs),
          companyBankAccountDetails = ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs)
        )
    }

    def error = throw fail("VatFinancials")

    implicit val apiT = new S4LApiTransformer[S4LVatFinancials, VatFinancials] {
      import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_YES
      // map S4LVatFinancials to VatFinancials
      override def toApi(c: S4LVatFinancials): VatFinancials =
        VatFinancials(
          bankAccount = c.companyBankAccountDetails.map(cad =>
            VatBankAccount(
              accountName = cad.accountName,
              accountSortCode = cad.sortCode,
              accountNumber = cad.accountNumber)),
          turnoverEstimate = c.estimateVatTurnover.map(_.vatTurnoverEstimate).getOrElse(error),
          zeroRatedTurnoverEstimate = c.zeroRatedTurnoverEstimate.map(_.zeroRatedTurnoverEstimate),
          reclaimVatOnMostReturns = c.vatChargeExpectancy.map(_.yesNo == VAT_CHARGE_YES).getOrElse(error),
          accountingPeriods = VatAccountingPeriod(
            frequency = c.vatReturnFrequency.map(_.frequencyType).getOrElse(MONTHLY),
            periodStart = c.vatReturnFrequency.flatMap(_ => c.accountingPeriod.map(_.accountingPeriod.toLowerCase())))
        )
    }
  }

}
